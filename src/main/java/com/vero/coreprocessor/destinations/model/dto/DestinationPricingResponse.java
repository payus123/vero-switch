package com.vero.coreprocessor.destinations.model.dto;

import com.vero.coreprocessor.destinations.enums.*;
import com.vero.coreprocessor.destinations.model.*;
import com.vero.coreprocessor.transactions.enums.*;
import lombok.*;

import java.math.*;
import java.time.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationPricingResponse {
    private String id;
    private BigDecimal value;
    private PricingType pricingType;
    private BigDecimal upperBound;
    private BigDecimal lowerBound;
    private TransactionType transactionType;
    private String currencyCode;

    private String destinationName;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public DestinationPricingResponse(DestinationPricing pricing) {
        this(
            pricing.getUuid(),
            pricing.getValue(),
            pricing.getPricingType(),
            pricing.getUpperBound(),
            pricing.getLowerBound(),
            pricing.getTransactionType(),
            pricing.getCurrencyCode(),
            pricing.getDestination(),
            pricing.getCreatedAt(),
            pricing.getModifiedAt()
        );
    }
}