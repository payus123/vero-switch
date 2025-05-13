package com.vero.coreprocessor.exceptions;

import lombok.*;
import org.springframework.http.*;

@Getter
public class CustomMessageException extends RuntimeException {
    private HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    public CustomMessageException(Exception exception) {
        super(exception.getLocalizedMessage());
    }

    public CustomMessageException(Exception exception, HttpStatus httpStatus) {
        super(exception.getLocalizedMessage());
        this.httpStatus = httpStatus;
    }
}
