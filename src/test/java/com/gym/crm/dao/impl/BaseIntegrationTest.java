package com.gym.crm.dao.impl;

import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.mysql.cj.jdbc.MysqlDataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

@ExtendWith(SpringExtension.class)
@Testcontainers
@ContextConfiguration(classes = {TraineeDAOImpl.class, TrainerDAOImpl.class, TrainingDAOImpl.class})
public abstract class BaseIntegrationTest<R> {
    private static final String DB_NAME = "gym_crm_test";
    private static final String USER = "test";
    private static final String PASSWORD = "test";

    @Container
    protected static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName(DB_NAME)
            .withUsername(USER)
            .withPassword(PASSWORD);

    protected static SessionFactory sessionFactory;
    private static MysqlDataSource dataSource;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    protected R dao;

    @BeforeAll
    static void setUpContainer() {
        mysql.start();

        dataSource = new MysqlDataSource();
        dataSource.setURL(mysql.getJdbcUrl());
        dataSource.setUser(mysql.getUsername());
        dataSource.setPassword(mysql.getPassword());

        sessionFactory = buildHibernateConfiguration().buildSessionFactory();
        new TransactionHandler(sessionFactory);
    }

    @AfterAll
    static void tearDownContainer() {
        if (sessionFactory != null) sessionFactory.close();
        mysql.stop();
    }

    @BeforeEach
    void resetDatabase() throws Exception {
        runLiquibaseMigrations(dataSource);
    }

    private static Configuration buildHibernateConfiguration() {
        Configuration configuration = new Configuration();

        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        configuration.setProperty("hibernate.connection.url", mysql.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", mysql.getUsername());
        configuration.setProperty("hibernate.connection.password", mysql.getPassword());

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "none");
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

    private static void runLiquibaseMigrations(MysqlDataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.dropAll();
            liquibase.update("test");
        }
    }

    protected <T> T doInSession(Function<Session, T> function) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            T result = function.apply(session);
            transaction.commit();

            return result;
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException("Error performing Hibernate operation. Transaction is rolled back", e);
        } finally {
            session.close();
        }
    }

    protected void doInSession(Consumer<Session> consumer) {
        doInSession(session -> {
            consumer.accept(session);

            return null;
        });
    }
}
