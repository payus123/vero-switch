package com.vero.coreprocessor.validations;

import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.terminals.model.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.springframework.stereotype.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeriveTransactionNameAndTypeImpl implements DeriveTransactionNameAndType, Constants {
    private final TerminalDAO terminalRepository;
    @Override
    public Context deriveTransactionNameAndType(Context context) {
        log.info("about deriving the transaction name and type");
        ISOMsg request = context.get(REQUEST);
        terminalRepository.getTerminal(request.getString(41)).ifPresentOrElse(terminal -> {
            log.info("terminal fetched successfully");
            checkTerminalCanTransact(terminal);
            context.put(TERMINAL,terminal);
            context.put(INSTITUTION,terminal.getMerchant().getInstitution());



            String txnNameAndType = request.getString(0).substring(1)+"."+request.getString(3).substring(0,2);

            context.put(TXNNAME,txnNameAndType);
        },() -> {
            log.error("Terminal not found");
            throw new TerminalException("terminal not found");
        });


        return context;
    }

    private void checkTerminalCanTransact(Terminal terminal){
        if(!terminal.getIsActive()){
            throw new TerminalException("terminal not active");
        }
    }
}
