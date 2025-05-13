package com.vero.coreprocessor.config;

import com.vero.coreprocessor.utils.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.context.support.*;

@Configuration
public class MessageSourceConfig {
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("language/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean(CommonConstants.Bean.CONSTANT_MESSAGE_SOURCE)
    public MessageSource constantMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("language/constants");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
