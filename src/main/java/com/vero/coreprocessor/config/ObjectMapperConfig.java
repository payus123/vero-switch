package com.vero.coreprocessor.config;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.*;
import jakarta.annotation.*;
import lombok.*;
import org.springframework.stereotype.*;

@AllArgsConstructor
@Component
public class ObjectMapperConfig {

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void updateObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
