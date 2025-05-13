package com.vero.coreprocessor.destinations.model.dto;

import com.vero.coreprocessor.destinations.enums.*;
import com.vero.coreprocessor.transactions.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.*;


@Getter
@Setter
public class DestinationPricingRequest {
    @NotNull
    @Positive
    private BigDecimal value;
    @NotNull
    private PricingType pricingType;
    @NotNull
    @Positive
    private BigDecimal upperBound;
    @NotNull
    private BigDecimal lowerBound;
    @NotNull
    private TransactionType transactionType;
    @NotBlank
    private String currencyCode;
    @NotNull
    private String destinationName;
}