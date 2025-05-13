package com.vero.coreprocessor.destinations.service;

import com.vero.coreprocessor.config.i18n.*;
import com.vero.coreprocessor.destinations.model.*;
import com.vero.coreprocessor.destinations.repository.*;
import com.vero.coreprocessor.exceptions.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InstitutionDestinationService {
    private final InstitutionDestinationRepository institutionDestinationRepository;
    private final DestinationRepository destinationRepository;

    public List<InstitutionDestination> getDestinationsByInstitution(String institutionId) {
        return institutionDestinationRepository.findByInstitutionId(institutionId);
    }

    @Transactional
    public InstitutionDestination attachDestinationToInstitution(String institutionId, Long destinationId) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new OmniproApplicationException(MessageCode.NOT_FOUND,"destination"));

        InstitutionDestination institutionDestination = InstitutionDestination.builder()
                .institutionId(institutionId)
                .destination(destination)
                .build();

        return institutionDestinationRepository.save(institutionDestination);
    }
}
