package com.vero.coreprocessor.exceptions;

import com.vero.coreprocessor.config.i18n.*;
import lombok.*;

@Getter
public class OmniproApplicationException extends RuntimeException {
    private final MessageCode messageCode;
    private final Object[] args;

    public OmniproApplicationException(MessageCode messageCode, Object ...args) {
        super(messageCode.getCode());
        this.messageCode = messageCode;
        this.args = args;

    }
}
