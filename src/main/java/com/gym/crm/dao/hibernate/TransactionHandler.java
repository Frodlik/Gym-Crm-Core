package com.gym.crm.dao.hibernate;

import com.gym.crm.exception.TransactionHandlerException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class TransactionHandler {
    private final SessionFactory sessionFactory;

    @Autowired
    public TransactionHandler(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public <T> T performReturningWithinSession(Function<Session, T> sessionFunction) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        try {
            T result = sessionFunction.apply(session);
            session.getTransaction().commit();
            return result;
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new TransactionHandlerException("Error performing Hibernate operation. Transaction is rolled back", e);
        } finally {
            session.close();
        }
    }
}
