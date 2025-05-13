package com.vero.coreprocessor.destinations.repository;


import com.vero.coreprocessor.destinations.model.*;
import com.vero.coreprocessor.transactions.enums.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface DestinationPricingRepository extends JpaRepository<DestinationPricing, Long> {
    Optional<DestinationPricing> findByUuid(String uuid);

    @Query("SELECT d FROM DestinationPricing d " + "WHERE d.transactionType = :transactionType " +
            "AND d.destination = :destination " + "AND d.currencyCode = :currencyCode")
    Optional<DestinationPricing> resolvePricing(
            @Param("transactionType") TransactionType transactionType,
            @Param("destination") String destination,
            @Param("currencyCode") String currencyCode);







}
