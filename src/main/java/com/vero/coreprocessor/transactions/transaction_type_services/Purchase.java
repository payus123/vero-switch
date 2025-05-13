package com.vero.coreprocessor.transactions.transaction_type_services;

import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.pintranslation.*;
import com.vero.coreprocessor.ruleengine.service.*;
import com.vero.coreprocessor.transactions.*;
import com.vero.coreprocessor.transactions.services.*;
import com.vero.coreprocessor.utils.*;
import lombok.*;
import org.jpos.iso.*;
import org.springframework.stereotype.*;

@Service("200.00")
@RequiredArgsConstructor
public class Purchase implements TransactionTypeSwitch, Constants {
    private final CheckFields checkFields;
    private final QueryHost queryHost;
    private final RuleEngine ruleEngine;
    private final MacUtils macUtils;
    private final PinTranslationService pinTranslationService;
    private final TransactionService transactionService;



    int[] mandatoryFields = {2,3,4,7,11,12,13,22,32,37,41,42,43};
    int[] optionalFields = {14,17,18,20,24,23,25,26,28,33,35,36,40,45,46,49,52,55,56,59,60,63,90,95,99,111,112,122,123,128};

    @Override
    public Context process(Context context) {
        ISOMsg req = Utils.getIsoMsgInstance(context.get(REQUEST));

        if(checkFields.validateFields(req,mandatoryFields,optionalFields)
                && macUtils.assertMac(req)
                && transactionService.checkDuplicateTransaction(req)){
            context.put(APPLY_FAILOVER, true);
            String destination = ruleEngine.getRoute(context);

            if (destination==null)
                throw new DestinationNotFoundException("destination not gotten for transaction");


            context.put(DESTINATION,destination);

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
