package com.vero.coreprocessor.destinations.model.dto;

import com.vero.coreprocessor.destinations.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDestination {
    @NotBlank
    private String destinationName;
    @NotNull
    @Positive
    private int port;
    @NotBlank
    private String ip;
    @Size(min = 32, max = 32, message = "ZPK must be of length 32")
    private String zpk;
    @Size(min = 32, max = 32, message = "ZMK must be of length 32")
    private String zmk;
    @NotNull
    private DestinationDomain domain;
}


