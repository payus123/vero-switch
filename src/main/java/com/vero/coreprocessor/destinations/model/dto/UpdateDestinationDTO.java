package com.vero.coreprocessor.destinations.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
public class UpdateDestinationDTO {
    @NotNull
    private String destinationName;
    private HashMap<String,Object> param;
}
