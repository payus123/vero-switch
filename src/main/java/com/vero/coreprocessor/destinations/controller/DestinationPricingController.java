package com.vero.coreprocessor.destinations.controller;

import com.vero.coreprocessor.destinations.model.dto.*;
import com.vero.coreprocessor.destinations.model.dto.filters.*;
import com.vero.coreprocessor.destinations.service.*;
import com.vero.coreprocessor.utils.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springdoc.core.annotations.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/destination-pricings")
@Tag(name = "DESTINATION PRICING MANAGEMENT")
@RequiredArgsConstructor
public class DestinationPricingController {

    private final DestinationPricingService destinationPricingService;

    @PostMapping
    public ResponseEntity<Wrapper<DestinationPricingResponse>> createDestinationPricing(
            @RequestBody @Valid DestinationPricingRequest request
    ) {
        log.info("Creating new destination pricing with request: {}", request);
        return ResponseBuilder.success(destinationPricingService.createDestinationPricing(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Wrapper<DestinationPricingResponse>> updateDestinationPricing(
            @PathVariable String id,
            @RequestBody @Validated DestinationPricingRequest request
    ) {
        log.info("Updating destination pricing with ID: {}", id);
        return ResponseBuilder.success(destinationPricingService.updateDestinationPricing(id, request));
    }

    @GetMapping
    public ResponseEntity<Wrapper<Collection<DestinationPricingResponse>>> getAllDestinationPricings(
            @ParameterObject Pageable pageable,
            @ParameterObject DestinationPricingFilter filter
    ) {
        log.info("Fetching all destination pricings with filter: {}", filter);
        return ResponseBuilder.success(destinationPricingService.getAllDestinationPricings(pageable, filter));
    }
}
