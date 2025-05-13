package com.vero.coreprocessor.transactions.transaction_type_services;

import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.keymodule.service.*;
import com.vero.coreprocessor.transactions.*;
import com.vero.coreprocessor.utils.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.springframework.stereotype.*;

@Service("800.9G")
@RequiredArgsConstructor
@Slf4j
public class PinKeyExchange implements TransactionTypeSwitch, Constants {
    private final CheckFields checkFields;
    private final KeyExchangeService keyExchangeService;
    int[] mandatoryFields = {3,7,11,12,13,41};
    int[] optionalFields = {62,63};
    @Override
    public Context process(Context context) {
        ISOMsg req = Utils.getIsoMsgInstance(context.get(REQUEST));
        if(checkFields.validateFields(req,mandatoryFields,optionalFields)){
            return keyExchangeService.doPinKey(context);
        }
        else {
            req.set("39","97");
            try {
                req.setResponseMTI();
            } catch (ISOException e) {
                throw new RuntimeException(e);
            }
            context.put(RESPONSE,req);
        }

        return context;
    }
}
