package com.gym.crm.exception;

public class TransactionHandlerException extends RuntimeException {
    public TransactionHandlerException(String message) {
        super(message);
    }

    public TransactionHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
