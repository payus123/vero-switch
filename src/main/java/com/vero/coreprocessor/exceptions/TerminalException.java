package com.vero.coreprocessor.exceptions;

public class TerminalException extends RuntimeException {
    public TerminalException() {
    }

    public TerminalException(String message) {
        super(message);
    }
}
