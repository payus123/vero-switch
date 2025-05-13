package com.vero.coreprocessor.destinations.repository;

import com.vero.coreprocessor.destinations.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstitutionDestinationRepository extends JpaRepository<InstitutionDestination, Long> {

    @EntityGraph(attributePaths = {"destination"})
    List<InstitutionDestination> findByInstitutionId(String institutionId);

    boolean existsByInstitutionIdAndDestination(String institutionId, Destination destination);



}
