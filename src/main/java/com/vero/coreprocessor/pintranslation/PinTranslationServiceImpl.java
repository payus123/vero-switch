package com.vero.coreprocessor.pintranslation;

import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.destinations.model.*;
import com.vero.coreprocessor.destinations.repository.*;
import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.keymodule.model.*;
import com.vero.coreprocessor.keymodule.repository.*;
import com.vero.coreprocessor.utils.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PinTranslationServiceImpl implements PinTranslationService, Constants {
    private final CryptoUtil cryptoUtil;
    private final TerminalKeysRepository terminalKeysRepository;
    private final DestinationRepository destinationRepository;
    @Override
    public Context translatePin(Context context) {
        ISOMsg req =context.get(REQUEST);
        Random random = new Random();

        String destinationName = context.get(DESTINATION);
        if (!req.hasField(52)){
            return context;
        }

        Destination destination = destinationRepository.findByDestinationName(destinationName).orElseThrow(() -> {
            log.error("Destination not gotten for pin translation {}", destinationName);
            return new DestinationNotFoundException("destination not gotten for pin translation");
        });

        if (destination.getZpk().isEmpty()){
            log.error("destination ZPK not gotten for destination {}",destinationName);
            throw new DestinationNotFoundException("Destination ZPK not gotten for "+destinationName);
        }

        String pin = req.getString(52);
        TerminalKeys terminalKeys = terminalKeysRepository.findByTerminalId(req.getString(41)).orElseThrow(()->{
            log.error("terminal key not gotten. attenpt key exchange for terminal {}",req.getString(41));
            return new EntityNotFoundException("Terminal Key not gotten for terminal id " + req.getString(41));
        });

        if (terminalKeys.getTpk().isEmpty()){
            log.error("perform master key exchange for terminal id {}",req.getString(41));
            throw new EntityNotFoundException("pin key not found for terminal id "+req.getString(41));
        }
        String translatePin = cryptoUtil.translatePin(pin, terminalKeys.getTpk(), destination.getZpk());
        req.unset(52);
        req.set(52,translatePin);
        log.info("pin translated from {} to {}",pin,translatePin);

//        if (destination.getDestinationName().equalsIgnoreCase("nibss")){
//            Institution institution = context.get(INSTITUTION);
//            List<String> tids = institution.getNibssTids();
//            if (tids.isEmpty()){
//                throw new TerminalException("No nibss tid found for institution: "+ institution.getInstitutionId());
//            }
//            String terminal = tids.get(random.nextInt(tids.size()));
//            req.set(59,terminal);
//
//        }todo will use when we assigns nibss tids to institutions

        return context;
    }
}
