package com.vero.coreprocessor.exceptions;

public class DestinationNotConnectedException extends RuntimeException {
    public DestinationNotConnectedException() {
    }

    public DestinationNotConnectedException(String message) {
        super(message);
    }
}
