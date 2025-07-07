package com.gym.crm.dao.impl;

import com.gym.crm.dao.hibernate.TransactionHandler;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class BaseIntegrationTest {
    private static final String DB_NAME = "gym_crm_test";
    private static final String USER = "test";
    private static final String PASSWORD = "test";
    private static final String INIT_SCRIPT = "init-test-db.sql";

    @Container
    protected static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName(DB_NAME)
            .withUsername(USER)
            .withPassword(PASSWORD)
            .withInitScript(INIT_SCRIPT);

    protected static SessionFactory sessionFactory;

    @BeforeAll
    static void setUpBase() {
        mysql.start();

        Configuration configuration = new Configuration();

        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        configuration.setProperty("hibernate.connection.url", mysql.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", mysql.getUsername());
        configuration.setProperty("hibernate.connection.password", mysql.getPassword());

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

        sessionFactory = configuration.buildSessionFactory();
        new TransactionHandler(sessionFactory);
    }

    @AfterAll
    static void tearDownBase() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        mysql.stop();
    }
}
