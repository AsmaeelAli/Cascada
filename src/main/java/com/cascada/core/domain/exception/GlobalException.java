package com.cascada.core.domain.exception;

public abstract class GlobalException extends RuntimeException {

    public GlobalException(String message) {
        super(message);
    }

    public GlobalException(String message, Throwable cause) {
        super(message, cause);
    }
}