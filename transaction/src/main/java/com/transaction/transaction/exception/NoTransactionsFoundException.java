package com.transaction.transaction.exception;

public class NoTransactionsFoundException extends RuntimeException {
    public NoTransactionsFoundException(String message) {
        super(message);
    }
}