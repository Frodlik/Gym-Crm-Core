package com.gym.crm.service.transaction;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
public class TransactionAspect {
    private SessionFactory sessionFactory;
    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Around("@annotation(persistenceTx)")
    public Object handleTransaction(ProceedingJoinPoint joinPoint, PersistenceTx persistenceTx) throws Throwable {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            if (persistenceTx.readOnly()) {
                session.setDefaultReadOnly(true);
            }

            Object result = joinPoint.proceed();
            transaction.commit();

            return result;
        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                if (shouldRollback(e, persistenceTx.rollbackFor())) {
                    transaction.rollback();
                } else {
                    transaction.commit();
                }
            }
            throw e;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    private boolean shouldRollback(Exception e, String[] rollbackFor) {
        return rollbackFor.length == 0 || Arrays.stream(rollbackFor)
                .anyMatch(name -> e.getClass().getSimpleName().equals(name));
    }
}
