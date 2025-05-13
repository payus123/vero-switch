package com.vero.coreprocessor.destinations.service;

import com.vero.coreprocessor.transactions.utilities.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DestinationRecoveryService {

    private final ProcessorFailoverManager processorFailoverManager;


    @Scheduled(fixedRateString = "${destination.recovery.interval.ms:1800000}")
    public void scheduleRecovery() {
        log.info("Scheduled processor recovery triggered.");
        processorFailoverManager.attemptRecoveryForAllInstitutions();
    }

    public void manualRecovery() {
        log.info("Manual processor recovery triggered via API.");
        processorFailoverManager.attemptRecoveryForAllInstitutions();
    }
}
