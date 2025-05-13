package com.vero.coreprocessor.exceptions;

import lombok.*;
import org.springframework.http.*;

@Getter
public class WebServiceException extends RuntimeException {
	private final String message;
	private final HttpStatus httpStatus;
	private final Object response;

	public WebServiceException(String message, HttpStatus httpStatus) {
		this(message, httpStatus, null);
	}

	public WebServiceException(String message, HttpStatus httpStatus, Object response) {
		this.message = message;
		this.httpStatus = httpStatus;
		this.response = response;
	}
}
