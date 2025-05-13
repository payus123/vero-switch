package com.vero.coreprocessor.destinations.service;

import com.vero.coreprocessor.destinations.model.dto.*;
import com.vero.coreprocessor.destinations.model.dto.filters.*;
import org.springframework.data.domain.*;

public interface DestinationPricingService {
    Page<DestinationPricingResponse> getAllDestinationPricings(Pageable pageable, DestinationPricingFilter filter);
    DestinationPricingResponse createDestinationPricing(DestinationPricingRequest request);
    DestinationPricingResponse updateDestinationPricing(String id, DestinationPricingRequest request);
}
