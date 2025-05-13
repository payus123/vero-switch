package com.vero.coreprocessor.destinations.repository;

import com.vero.coreprocessor.destinations.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface DestinationRepository extends JpaRepository<Destination,Long> {
    Optional<Destination> findByDestinationName(String destinationName);

    List<Destination> findAllByStatus(Boolean status);
}
