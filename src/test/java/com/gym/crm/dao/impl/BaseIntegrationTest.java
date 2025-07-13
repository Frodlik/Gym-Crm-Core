package com.gym.crm.dao.impl;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.junit5.api.DBRider;
import com.gym.crm.config.TestDataSourceConfig;
import com.mysql.cj.jdbc.MysqlDataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

@DBRider
@ExtendWith(SpringExtension.class)
@Testcontainers
@ContextConfiguration(classes = {TraineeDAOImpl.class,
        TrainerDAOImpl.class,
        TrainingDAOImpl.class,
        TestDataSourceConfig.class})
@DBUnit(leakHunter = false, schema = "gym_crm_test", caseSensitiveTableNames = true)
public abstract class BaseIntegrationTest<R> {
    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    protected R dao;

    @Autowired
    private MysqlDataSource dataSource;

    @BeforeEach
    void resetDatabase() throws Exception {
        runLiquibaseMigrations(dataSource);
    }

    @AfterEach
    void tearDownTransaction() {
        Session session = sessionFactory.getCurrentSession();
        if (session.getTransaction().isActive()) {
            session.getTransaction().rollback();
        }

        if (session.isOpen()) {
            session.close();
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

    private void runLiquibaseMigrations(MysqlDataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );
            liquibase.dropAll();
            liquibase.update();
        }
    }
}
