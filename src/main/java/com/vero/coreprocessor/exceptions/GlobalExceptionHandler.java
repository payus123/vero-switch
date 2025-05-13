package com.vero.coreprocessor.exceptions;


import com.vero.coreprocessor.config.i18n.*;
import com.vero.coreprocessor.utils.ErrorResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.boot.web.servlet.error.*;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.validation.*;
import org.springframework.web.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

;

@RestController
@RestControllerAdvice
public class GlobalExceptionHandler implements ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(OmniproApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(OmniproApplicationException e, Locale locale) {
        final String message = messageSource.getMessage(e.getMessageCode().getCode(), e.getArgs(), locale);
        return ResponseEntity.status(e.getMessageCode().getHttpStatus()).body(new ErrorResponse(e.getMessageCode().getHttpStatus(), message));
    }

    @ExceptionHandler(CustomMessageException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(CustomMessageException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getHttpStatus(), e.getLocalizedMessage()));
    }

    @ExceptionHandler(WebServiceException.class)
    public ResponseEntity<ErrorResponse> handleWebServiceException(WebServiceException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getHttpStatus(), e.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleError(MissingRequestHeaderException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getLocalizedMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleError(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ErrorResponse(e.getLocalizedMessage()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleError(BindException e, Locale locale) {

        Map<String, String> reasons = new HashMap<>();

        e.getBindingResult().getAllErrors().parallelStream().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();

            synchronized (reasons) {
                reasons.put(fieldName, fieldName + " " + error.getDefaultMessage());
            }
        });

        String message = messageSource.getMessage(MessageCode.UNPROCESSABLE_ENTITY.getCode(), null, locale);

        return ResponseEntity.unprocessableEntity().body(new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, message, reasons));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleError(HttpMessageNotReadableException e) {
        String errorMessage = e.getLocalizedMessage();
        logger.error(errorMessage, e);

        String[] split = errorMessage != null ? errorMessage.split(":") : null;

        return ResponseEntity.badRequest().body(new ErrorResponse((split == null || split.length == 0) ? errorMessage : split[0]));
    }
    

    public static HttpStatus getHttpStatus(HttpServletRequest request, boolean defaultTo500) {

        String code = request.getParameter("code");
        Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        HttpStatus httpStatus;

        if (status != null) httpStatus = HttpStatus.valueOf(status);
        else if (code != null && code.length() > 0) httpStatus = HttpStatus.valueOf(code);
        else httpStatus = defaultTo500 ? HttpStatus.INTERNAL_SERVER_ERROR : null;

        return httpStatus;
    }

    private ErrorResponse getErrorResponse(HttpServletRequest request, HttpStatus httpStatus, Locale locale) {
        ErrorResponse errorResponse;

        String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        switch (httpStatus) {
            case NOT_FOUND -> {
                return new ErrorResponse(HttpStatus.NOT_FOUND, messageSource.getMessage(MessageCode.NOT_FOUND.getCode(),
                        new String[] {ConstantCode.ROUTE_ENTITY.getValue()},
                        locale));
            }
            case INTERNAL_SERVER_ERROR -> errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, messageSource.getMessage(MessageCode.INTERNAL_SERVER_ERROR.getCode(), null, locale));
            case FORBIDDEN -> {
                if (message == null || message.equals("Forbidden"))
                    errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN, messageSource.getMessage(MessageCode.FORBIDDEN.getCode(), null, locale));
                else
                    errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN, message);
            }
            case UNAUTHORIZED -> {
                if (message == null)
                    message = messageSource.getMessage(MessageCode.UNAUTHORIZED.getCode(), null, locale);

                return new ErrorResponse(HttpStatus.UNAUTHORIZED, message);
            }
            default ->
                    errorResponse = new ErrorResponse(httpStatus, (message != null && !message.isEmpty()) ? message : httpStatus.getReasonPhrase());
        }

        return errorResponse;
    }
}
