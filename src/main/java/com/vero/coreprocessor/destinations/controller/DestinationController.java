package com.vero.coreprocessor.destinations.controller;

import com.vero.coreprocessor.destinations.dtos.*;
import com.vero.coreprocessor.destinations.model.*;
import com.vero.coreprocessor.destinations.model.dto.*;
import com.vero.coreprocessor.destinations.service.*;
import com.vero.coreprocessor.utils.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springdoc.core.annotations.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/destinations")
@Tag(name = "DESTINATION MANAGEMENT")
public class DestinationController {
    private final DestinationService destinationService;
    private final DestinationRecoveryService destinationRecoveryService;


    @PostMapping()
    public ResponseEntity<Wrapper<DestinationResponse>> createDestination(@RequestBody @Valid RegisterDestination request){
            log.info("create destination method");
            return ResponseBuilder.success(destinationService.registerDestination(request));
    }

    @GetMapping("/{institutionId}")
    public ResponseEntity<Wrapper<List<InstitutionDestination>>> getDestinationsByInstitution(@PathVariable String institutionId) {
        log.info("Fetching destinations for institution: {}", institutionId);
        return ResponseBuilder.success(destinationService.getDestinationsByInstitution(institutionId));
    }

    @PatchMapping("/attach/{institutionId}/{destinationName}")
    public ResponseEntity<Wrapper<DestinationResponse>> attachDestinationToInstitution(
            @PathVariable String institutionId,
            @PathVariable String destinationName) {
        log.info("Attaching destination {} to institution {}", destinationName, institutionId);
        return ResponseBuilder.success(destinationService.attachDestinationToInstitution(institutionId, destinationName));
    }
    @PatchMapping()
    public ResponseEntity<Wrapper<DestinationResponse>> updateDestination(@RequestBody @Valid UpdateDestinationDTO request){
            log.info("update destination method");
            return ResponseBuilder.success(destinationService.updateDestination(request));
    }

    @GetMapping()
    public ResponseEntity<Wrapper<Collection<DestinationResponse>>> viewAllDestinations(@ParameterObject Pageable pageable, @ParameterObject DestinationFilter filter){
            log.info("view all destination method");
            return ResponseBuilder.success(destinationService.viewAllDestinations(pageable,filter));
    }


    @GetMapping("/{name}")
    public ResponseEntity<Wrapper<DestinationResponse>> viewByName(@PathVariable("name")String name){
            log.info("view destination by name method");
            return ResponseBuilder.success(destinationService.viewByName(name));
    }

    @GetMapping("/get-by-status")
    public ResponseEntity<Wrapper<List<DestinationResponse>>> viewByStatus(@RequestBody boolean status){
            log.info("view destination by status method");
            return  ResponseBuilder.success(destinationService.viewByStatus(status));

    }

    @GetMapping("/key-exchange/{destination}")
    public ResponseEntity<?> doDestinationKeyExchange(@PathVariable("destination") String destination){
            log.info("do destination key exchange method");
            return ResponseBuilder.success(destinationService.doDestinationKeyExchange(destination));
    }


    @PostMapping("inject-zpk")
    public ResponseEntity<?> setDestinationZpk(@RequestBody HashMap<String,String> request){
            log.info("do destination key exchange method");
            return new ResponseEntity<>(destinationService.setDestinationZpk(request), HttpStatus.OK);
    }

    @PostMapping("inject-destination-zmk-components")
    public ResponseEntity<?> injectDestinationComponents(@RequestBody HashMap<String,String> request){
            log.info("do destination key exchange method");
            return new ResponseEntity<>(destinationService.injectDestinationComponents(request), HttpStatus.OK);
    }


    @PostMapping("/recovery/trigger")
    public ResponseEntity<Wrapper<String>> triggerRecoveryManually() {
        log.info("Manual recovery endpoint called.");
        destinationRecoveryService.manualRecovery();
        return ResponseBuilder.success("Recovery triggered successfully");
    }
}
