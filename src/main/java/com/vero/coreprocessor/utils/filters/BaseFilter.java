package com.vero.coreprocessor.utils.filters;


import com.vero.coreprocessor.config.*;
import com.vero.coreprocessor.config.i18n.*;
import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.utils.*;
import io.vavr.control.*;
import lombok.*;
import org.apache.commons.lang3.*;
import org.springframework.context.*;
import org.springframework.context.i18n.*;

import java.time.*;
import java.time.format.*;
import java.util.*;

@Getter
@Setter
public abstract class BaseFilter {
    private String from;
    private String to;

    private final MessageSource messageSource;
    private final Locale locale;

    public BaseFilter() {
        messageSource = SpringContextConfig.getBean(MessageSource.class, CommonConstants.Bean.CONSTANT_MESSAGE_SOURCE);
        locale = LocaleContextHolder.getLocale();
    }

    public <T, D> void filter(final QueryBuilder<T, D> queryBuilder) {
        queryBuilder.defaultPredicate();

        if (!StringUtils.isEmpty(from) && !StringUtils.isEmpty(to)) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CommonConstants.Pattern.DATE_FORMAT);
            Try.of(() -> {
                final ZonedDateTime fromDate = LocalDate.parse(from, formatter).atStartOfDay(ZoneId.systemDefault());
                final ZonedDateTime toDate = ZonedDateTime.of(LocalDate.parse(to, formatter).atTime(LocalTime.MAX), ZoneId.systemDefault());

                return queryBuilder.between("createdAt", fromDate, toDate);
            }).getOrElseThrow(this::getDateRangeError);
        }
    }

    protected OmniproApplicationException getDateRangeError() {
        final String fromKey = messageSource.getMessage(ConstantCode.FROM.getValue(), null, locale);
        final String toKey = messageSource.getMessage(ConstantCode.TO_FIELD.getValue(), null, locale);

        return new OmniproApplicationException(MessageCode.DATE_RANGE, fromKey, toKey, CommonConstants.Pattern.DATE_FORMAT);
    }

}
