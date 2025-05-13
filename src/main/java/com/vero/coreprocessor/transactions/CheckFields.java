package com.vero.coreprocessor.transactions;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.springframework.stereotype.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckFields {

    public boolean validateFields(ISOMsg req,int[]mandatoryFields,int[]optionalFields){

        if (req.hasFields(mandatoryFields)){
            log.info("mandatory fields present");
            StringBuilder missingFields = new StringBuilder();
            for (int i = 0; i < optionalFields.length; i++) {
                if (!req.hasField(optionalFields[i])){
                   missingFields.append(optionalFields[i]).append(",");
                }
            }
            log.info("optional fields not present is {}", missingFields);
            return true;
        }
        else
            return false;
    }
}
