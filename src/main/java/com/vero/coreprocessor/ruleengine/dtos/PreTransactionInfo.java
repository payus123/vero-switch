package com.vero.coreprocessor.ruleengine.dtos;


import com.vero.coreprocessor.transactions.enums.*;
import lombok.*;

import java.math.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreTransactionInfo {
    private boolean isInternational;
    private String merchantType;
    private BigDecimal charges = BigDecimal.ZERO;
    private BigDecimal amount = BigDecimal.ZERO;
    private String currency;
    private TransactionType transactionType;
}
