package com.vero.coreprocessor.destinations.service;

import com.vero.coreprocessor.destinations.dtos.*;
import com.vero.coreprocessor.destinations.model.*;
import com.vero.coreprocessor.destinations.model.dto.*;
import org.springframework.data.domain.*;

import java.util.*;

public interface DestinationService {
    DestinationResponse registerDestination(RegisterDestination request);

    DestinationResponse updateDestination(UpdateDestinationDTO request);

    Page<DestinationResponse> viewAllDestinations(Pageable pageable, DestinationFilter destinationFilter);

    DestinationResponse viewDestinationById(Long id);

    DestinationResponse viewByName(String name);

    List<DestinationResponse> viewByStatus(boolean value);

    Object doDestinationKeyExchange(String destination);

    Object setDestinationZpk(HashMap<String, String> request);

    Object injectDestinationComponents(HashMap<String, String> request);

    List<InstitutionDestination> getDestinationsByInstitution(String institutionId);

    DestinationResponse attachDestinationToInstitution(String institutionId, String destinationName);

}
