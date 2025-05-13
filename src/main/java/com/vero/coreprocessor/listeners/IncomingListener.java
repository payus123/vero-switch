package com.vero.coreprocessor.listeners;

import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.notifications.service.*;
import com.vero.coreprocessor.transactions.*;
import com.vero.coreprocessor.transactions.model.*;
import com.vero.coreprocessor.transactions.services.*;
import com.vero.coreprocessor.validations.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static com.vero.coreprocessor.utils.Utils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncomingListener implements ISORequestListener, Constants {
    private final DeriveTransactionNameAndType deriveTransactionNameAndType;
    private final Switch transactionTypeSwitch;
    private final TransactionService transactionService;
    ExecutorService executorService = Executors.newCachedThreadPool();
    private final NotificationService notificationService;

    @Value("${spring.profiles.active}")
    String environment;

    @Override
    public boolean process(ISOSource source, ISOMsg isoMsg) {
        Context context = new Context();
        context.put(INCOMING_REQUEST, getIsoMsgInstance(isoMsg));
        context.put(REQUEST, getIsoMsgInstance(isoMsg));
        context.put(CHARGE, 0d);
        ISOMsg resp = (ISOMsg) isoMsg.clone();
        context.put(INCOMING_TIME_STAMP, new Date());
        Transaction transaction = null;

        try {
            context = deriveTransactionNameAndType.deriveTransactionNameAndType(context);
            context = transactionTypeSwitch.switchTransactionType(context);
            resp = context.get(RESPONSE);
            if (!resp.isResponse()){
                resp.setResponseMTI();

            }

             transaction = transactionService.saveTransaction(context, resp);
//            transactionService.postProcess(transaction);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error caught is {}", e.getMessage());
            if (context.hasKey("RESPONSE"))
                resp = context.get("RESPONSE");

            if (!resp.hasField(39)) {
                resp.set("39", "91");
            }
            context.put(RESPONSE, resp);
            throw new IncomingListenerException(e);
        } finally {
            try {
                if (!resp.isResponse()) {
                    ISOMsg response = (ISOMsg) resp.clone();
                    response.setResponseMTI();
                    response.unset(52);
                    context.put(RESPONSE, response);
                }
                source.send(context.get(RESPONSE));
                notificationService.notifyTransactionFailure(transaction);
                context.put(OUTGOING_TIME_STAMP, new Date());
                context.dump(System.out, "", environment);
            } catch (IOException | ISOException e) {
                log.error("error {}", e.getMessage());
            }
        }
        return true;
    }
}
