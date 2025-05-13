package com.vero.coreprocessor.config.i18n;


import com.vero.coreprocessor.config.*;
import com.vero.coreprocessor.utils.*;
import org.springframework.context.*;
import org.springframework.context.i18n.*;

public enum ConstantCode {
    // entities
    ROUTE_ENTITY("entity.route"),
    FROM("field.from"),
    TO_FIELD("field.to");

    final String code;

    ConstantCode(String code) {
        this.code = code;
    }

    public String getValue() {
        final MessageSource messageSource = SpringContextConfig.getBean(MessageSource.class, CommonConstants.Bean.CONSTANT_MESSAGE_SOURCE);
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
