package com.vero.coreprocessor.config.i18n;

import lombok.*;
import org.springframework.http.*;

@Getter
public enum MessageCode {
    // error messages
    INTERNAL_SERVER_ERROR("error.server_error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("error.server_unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    BAD_REQUEST("error.bad_request", HttpStatus.BAD_REQUEST),
    UNPROCESSABLE_ENTITY("error.unprocessable_entity", HttpStatus.UNPROCESSABLE_ENTITY),
    UNAUTHORIZED("error.unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("error.forbidden", HttpStatus.FORBIDDEN),

    // Validation error messages
    NOT_FOUND("validation.not_found", HttpStatus.NOT_FOUND),
    NOT_EXIST("validation.exists", HttpStatus.BAD_REQUEST),
    UNIQUE("validation.unique", HttpStatus.CONFLICT),
    MISSING_KYB_DOCUMENTS("validation.missing_kyb_documents", HttpStatus.BAD_REQUEST),
    MISSING_KYC_DOCUMENTS("validation.missing_kyc_documents", HttpStatus.BAD_REQUEST),
    NOT_VERIFIED("validation.not_verified", HttpStatus.BAD_REQUEST),
    MISSING_KYC_DTO("validation.missing_kyc_dto", HttpStatus.BAD_REQUEST),
    MISSING_BVN("validation.missing_bvn", HttpStatus.BAD_REQUEST),
    MISSING_POA("validation.missing_poa", HttpStatus.BAD_REQUEST),
    LINK_NOT_FOUND("validation.link_not_found", HttpStatus.BAD_REQUEST),
    DATE_RANGE("validation.invalid_date_range", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD("validation.password", HttpStatus.BAD_REQUEST),
    IMMUTABLE_ROLE("validation.role.update", HttpStatus.BAD_REQUEST),

    DUPLICATE_ENTITY("general.duplicate_entity", HttpStatus.BAD_REQUEST),
    INVALID_FIELD("general.invalid_field", HttpStatus.BAD_REQUEST),
    MISSING_ENTITY("general.missing_entity", HttpStatus.BAD_REQUEST),
    OPERATION_SUCCESSFUL("notification.operation_successful", HttpStatus.OK);


    final String code;
    final HttpStatus httpStatus;

    MessageCode(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
