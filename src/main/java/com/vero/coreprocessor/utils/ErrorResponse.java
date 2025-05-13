package com.vero.coreprocessor.utils;

import lombok.*;
import org.springframework.http.*;

import java.util.*;

@Getter
public class ErrorResponse {
    private final Error error;

    public ErrorResponse(String message) {
        this(HttpStatus.BAD_REQUEST, message, new HashMap<>());
    }
    public ErrorResponse(HttpStatus status, String message) {
        this(status, message, new HashMap<>());
    }

    public ErrorResponse(HttpStatus status, String message, Map<String, String> reasons) {
        this.error = new Error(status, message, reasons);
    }

    public record Error(HttpStatus status, String message, Map<String, String> reasons) {
        public Error(HttpStatus status, String message, Map<String, String> reasons) {
            this.status = status;
            this.message = message;
            this.reasons = reasons == null ? new HashMap<>() : reasons;
        }
    }
}
