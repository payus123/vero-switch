package com.vero.coreprocessor.destinations.model;

import com.vero.coreprocessor.destinations.enums.*;
import com.vero.coreprocessor.transactions.enums.*;
import com.vero.coreprocessor.utils.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationPricing extends BaseModel {
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    private PricingType pricingType;

    private BigDecimal upperBound;

    private BigDecimal lowerBound;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private String currencyCode;

    private String destination;


}
