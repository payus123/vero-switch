package com.vero.coreprocessor.config;

import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.*;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient(){
        return WebClient.builder().build();
    }
}
