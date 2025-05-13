package com.vero.coreprocessor.utils;

import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.transactions.model.*;
import com.vero.coreprocessor.transactions.services.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.springframework.stereotype.*;

import static com.vero.coreprocessor.constants.Constants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReversalValidator {

    private final TransactionService transactionService;


    public String validateAndGetProcessor(Context context) {
        ISOMsg msg = context.get(REQUEST);

        try {
            if (!msg.hasField(90) || !msg.hasField(95)) {
                msg.set(39, "30");
                throw new CoreProcessorException("Field 95 or field 90 not present");
            }
            String field90 = msg.getString(90);
            String field95 = msg.getString(95);

            String mti = getResponseMTI(field90.substring(0, 4));
            String originalTransmissionDateTime = field90.substring(10, 20);
            String stan = field90.substring(4, 10);

            log.info("mti {} originalTransmissionDateTime {}, stan{}", mti, originalTransmissionDateTime, stan);
            Transaction originalTransaction = transactionService.findOriginalTransaction(stan, mti, originalTransmissionDateTime);

            if (originalTransaction == null) {
                log.error("Original transaction not found");
                msg.set(39, "25");
                throw new CoreProcessorException("Old transaction not found");
            }

            if (!validateField90(field90, originalTransaction) || !validateField95(field95)) {
                msg.set(39, "30");
                log.error("error validating field 90 or 95");
                throw new CoreProcessorException("Invalid de90 or de95");
            }
            return originalTransaction.getDestination();
        } catch (Exception e) {
            context.put(RESPONSE, msg);
            log.info("Message response code is {}", msg.getString(39));
            throw new CoreProcessorException("Error validate reversal: " + e.getMessage());
        }

    }

    private boolean validateField90(String field90, Transaction oldTransaction) {
        String originalMessageType = getResponseMTI(field90.substring(0, 4));
        String originalTransmissionDateTime = field90.substring(10, 20);
        String originalSystemsTraceAuditNumber = field90.substring(4, 10);

        if (!originalMessageType.equals(oldTransaction.getMti())) {
            log.error("Original Message Type mismatch with old transaction");
            return false;
        }

        if (!originalSystemsTraceAuditNumber.equals(oldTransaction.getStan())) {
            log.error("Original Systems Trace Audit Number mismatch with old transaction");
            return false;
        }


        if (!originalTransmissionDateTime.equals(oldTransaction.getTranDateAndTime().substring(0, 10))) {
            log.error("Original Transmission Date and Time mismatch with old transaction");
            return false;
        }
        String originalAcquirerInstitutionId = field90.substring(20, 31);
        originalAcquirerInstitutionId = originalAcquirerInstitutionId.substring(originalAcquirerInstitutionId.length() - 6);
        if (!originalAcquirerInstitutionId.equals(oldTransaction.getAcquiringInstitutionID())) {
            log.error("Original Acquirer Institution ID mismatch with old transaction");
            return false;
        }

        return true;

    }

    private boolean validateField95(String field95) {

        String transactionFee = field95.substring(24, 33);
        if (transactionFee.charAt(0) != 'D' && transactionFee.charAt(0) != 'C') {
            log.error("Invalid Actual Amount, Transaction Fee in Field 95");
            return false;
        }

        String settlementFee = field95.substring(33, 42);
        if (settlementFee.charAt(0) != 'D' && settlementFee.charAt(0) != 'C') {
            log.error("Invalid Actual Amount, Settlement Fee in Field 95");
            return false;
        }

        return true;

    }

    @SneakyThrows
    public String getResponseMTI(String mti) {
        char c1 = mti.charAt(3);  // The last character of the MTI
        char c2 = switch (c1) {
            case '2', '3' -> '2';
            case '4', '5' -> '4';
            default -> '0';

        };

        return mti.substring(0, 2) + (Character.getNumericValue(mti.charAt(2)) + 1) + c2;
    }

}
