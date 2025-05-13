package com.vero.coreprocessor.exceptions;

public class StatusCheckException extends RuntimeException {
    public StatusCheckException() {
    }

    public StatusCheckException(String message) {
        super(message);
    }
}
