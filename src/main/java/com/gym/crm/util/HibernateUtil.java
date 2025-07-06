package com.gym.crm.util;

import com.gym.crm.exception.HibernateUtilException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class HibernateUtil {
    private static SessionFactory sessionFactory;

    @Autowired
    public HibernateUtil(SessionFactory sessionFactory) {
        HibernateUtil.sessionFactory = sessionFactory;
    }

    public static <T> T performReturningWithinSession(Function<Session, T> sessionFunction) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        try {
            T result = sessionFunction.apply(session);
            session.getTransaction().commit();
            return result;
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new HibernateUtilException("Error performing Hibernate operation. Transaction is rolled back", e);
        } finally {
            session.close();
        }
    }
}
