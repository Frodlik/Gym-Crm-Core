package com.gym.crm.exception;

public class HibernateUtilException extends RuntimeException {
    public HibernateUtilException(String message) {
        super(message);
    }

    public HibernateUtilException(String message, Throwable cause) {
        super(message, cause);
    }
}
