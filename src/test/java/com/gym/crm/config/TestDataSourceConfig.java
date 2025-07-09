package com.gym.crm.config;

import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;

@ComponentScan(basePackages = "com.gym.crm.dao")
@Configuration
public class TestDataSourceConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("gym_crm_test")
                .withUsername("test")
                .withPassword("test");
    }

    @Bean
    public DataSource dataSource(MySQLContainer<?> mysqlContainer) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(mysqlContainer.getJdbcUrl());
        dataSource.setUser(mysqlContainer.getUsername());
        dataSource.setPassword(mysqlContainer.getPassword());

        return dataSource;
    }

    @Bean
    public SessionFactory sessionFactory(MySQLContainer<?> mysqlContainer) {
        return buildHibernateConfiguration(mysqlContainer).buildSessionFactory();
    }

    @Bean
    public TransactionHandler transactionHandler(SessionFactory sessionFactory) {
        return new TransactionHandler(sessionFactory);
    }

    private org.hibernate.cfg.Configuration buildHibernateConfiguration(MySQLContainer<?> mysqlContainer) {
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();

        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        configuration.setProperty("hibernate.connection.url", mysqlContainer.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", mysqlContainer.getUsername());
        configuration.setProperty("hibernate.connection.password", mysqlContainer.getPassword());

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");
        configuration.setProperty("hibernate.use_sql_comments", "true");
        configuration.setProperty("hibernate.current_session_context_class", "thread");
        configuration.setProperty("hibernate.connection.characterEncoding", "utf8");
        configuration.setProperty("hibernate.connection.CharSet", "utf8");
        configuration.setProperty("hibernate.connection.useUnicode", "true");

        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Trainee.class);
        configuration.addAnnotatedClass(Trainer.class);
        configuration.addAnnotatedClass(Training.class);
        configuration.addAnnotatedClass(TrainingType.class);

        return configuration;
    }
}
