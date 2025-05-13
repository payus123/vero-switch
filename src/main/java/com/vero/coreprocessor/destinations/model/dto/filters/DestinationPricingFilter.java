package com.vero.coreprocessor.destinations.model.dto.filters;

import com.vero.coreprocessor.transactions.enums.*;
import com.vero.coreprocessor.utils.*;
import com.vero.coreprocessor.utils.filters.*;
import io.micrometer.common.util.*;
import lombok.*;

@Getter
@Setter
public class DestinationPricingFilter extends BaseFilter {
    private String destination;
    private TransactionType transactionType;

    @Override
    public <T, D> void filter(QueryBuilder<T, D> queryBuilder) {
        super.filter(queryBuilder);

        if (transactionType != null) {
            queryBuilder.equal("transactionType", transactionType);
        }

        if (!StringUtils.isEmpty(destination)) {
            queryBuilder.equal("destination", destination);
        }
    }
}