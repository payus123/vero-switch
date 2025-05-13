package com.vero.coreprocessor.destinations.model;

import com.vero.coreprocessor.destinations.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.*;

@Data
public class PricingDto {
    @NotNull
    @Positive
    private BigDecimal value;
    @NotNull
    private PricingType pricingType;
    @NotNull
    @Positive
    private BigDecimal upperBound;
    @NotNull
    @Positive
    private BigDecimal lowerBound;
}
