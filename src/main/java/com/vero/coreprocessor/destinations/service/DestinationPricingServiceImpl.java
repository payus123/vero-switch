package com.vero.coreprocessor.destinations.service;

import com.vero.coreprocessor.config.i18n.*;
import com.vero.coreprocessor.destinations.model.*;
import com.vero.coreprocessor.destinations.model.dto.*;
import com.vero.coreprocessor.destinations.model.dto.filters.*;
import com.vero.coreprocessor.destinations.repository.*;
import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.utils.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;

@Service
@RequiredArgsConstructor
public class DestinationPricingServiceImpl implements DestinationPricingService {

    private final DestinationPricingRepository destinationPricingRepository;
    private final DestinationRepository destinationRepository;

    @Override
    public Page<DestinationPricingResponse> getAllDestinationPricings(Pageable pageable, DestinationPricingFilter filter) {
        QueryBuilder<DestinationPricing, DestinationPricingResponse> queryBuilder =
                QueryBuilder.build(DestinationPricing.class, DestinationPricingResponse.class);

        filter.filter(queryBuilder);
        queryBuilder.orderBy("createdAt", true);

        return queryBuilder.getResult(pageable);
    }



    @Override
    public DestinationPricingResponse createDestinationPricing(DestinationPricingRequest request) {
        Destination destination = destinationRepository.findByDestinationName(request.getDestinationName())
                .orElseThrow(() -> new OmniproApplicationException(MessageCode.NOT_FOUND, "Destination not found"));

        destinationPricingRepository.resolvePricing(request.getTransactionType(), destination.getDestinationName(),request.getCurrencyCode())
                .ifPresent(existing -> {
                    throw new OmniproApplicationException(MessageCode.UNIQUE, "Destination pricing already exists for this transaction type");
                });

        DestinationPricing pricing = DestinationPricing.builder()
                .value(request.getValue())
                .pricingType(request.getPricingType())
                .upperBound(request.getUpperBound())
                .lowerBound(request.getLowerBound())
                .transactionType(request.getTransactionType())
                .currencyCode(request.getCurrencyCode())
                .destination(destination.getDestinationName())
                .build();

        return new DestinationPricingResponse(destinationPricingRepository.save(pricing));
    }

    @Override
    public DestinationPricingResponse updateDestinationPricing(String id, DestinationPricingRequest request) {
        DestinationPricing pricing = destinationPricingRepository.findByUuid(id)
                .orElseThrow(() -> new OmniproApplicationException(MessageCode.NOT_FOUND, "Pricing not found"));

        pricing.setValue(request.getValue());
        pricing.setPricingType(request.getPricingType());
        pricing.setUpperBound(request.getUpperBound());
        pricing.setLowerBound(request.getLowerBound());
        pricing.setTransactionType(request.getTransactionType());
        pricing.setCurrencyCode(request.getCurrencyCode());

        return new DestinationPricingResponse(destinationPricingRepository.save(pricing));
    }
}