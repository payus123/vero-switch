package com.vero.coreprocessor.config;

import org.springdoc.core.models.*;
import org.springframework.context.annotation.*;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicAPI(){
        return GroupedOpenApi.builder()
                .group("spingshop-public")
                .pathsToMatch("/**/**")
                .build();
    }
}
