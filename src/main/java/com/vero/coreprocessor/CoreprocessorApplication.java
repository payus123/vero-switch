package com.vero.coreprocessor;

import io.swagger.v3.oas.annotations.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.scheduling.annotation.*;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition
@EnableAsync
public class CoreprocessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreprocessorApplication.class, args);
    }

}
