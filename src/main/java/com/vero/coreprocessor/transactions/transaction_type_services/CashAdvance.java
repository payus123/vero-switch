package com.vero.coreprocessor.transactions.transaction_type_services;

import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.pintranslation.*;
import com.vero.coreprocessor.ruleengine.service.*;
import com.vero.coreprocessor.transactions.*;
import com.vero.coreprocessor.utils.*;
import lombok.*;
import org.jpos.iso.*;
import org.springframework.stereotype.*;

@Service("200.01")
@RequiredArgsConstructor
public class CashAdvance implements TransactionTypeSwitch, Constants {
    private final CheckFields checkFields;
    private final QueryHost queryHost;
    private final RuleEngine ruleEngine;
    private final PinTranslationService pinTranslationService;

    int[] mandatoryFields = {2,3,4,7,11,12,13,22,32,41,42,43};
    int[] optionalFields = {14,17,20,24,23,25,26,33,35,36,40,45,46,49,52,55,59,60,63,111,112,122,123,128};

    @Override
    public Context process(Context context) {
        ISOMsg req = Utils.getIsoMsgInstance(context.get(REQUEST));
        if(checkFields.validateFields(req,mandatoryFields,optionalFields)){


            context.put(APPLY_FAILOVER, true);

            String destination = ruleEngine.getRoute(context);

            context.put(DESTINATION,destination);

            if (destination==null)
                throw new DestinationNotFoundException("destination not gotten for transaction");


            Context ctx = pinTranslationService.translatePin(context);
            return queryHost.prepare(ctx);
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
