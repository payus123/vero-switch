package com.vero.coreprocessor.config;

import org.modelmapper.*;
import org.modelmapper.convention.*;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.config.annotation.*;

import java.util.*;

@Configuration
public class GeneralBeanConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // Apply to all paths
                        .allowedOrigins("*")  // Allow all origins
                        .allowedMethods("*")  // Allow all HTTP methods
                        .allowedHeaders("*"); // Allow all headers
            }
        };
    }
}
