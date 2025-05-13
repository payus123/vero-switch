package com.vero.coreprocessor.transactions;

import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.exceptions.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.jpos.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryHostImpl implements QueryHost, Constants {
    @Value("${transactionTimeout}")
    Long transactionTimeout;

    @Override
    public Context prepare(Context context) {
        String destination = context.getString(DESTINATION);
        if (destination.isEmpty()) {
            log.error("destination not gotten");
            throw new DestinationNotFoundException("destination not gotten");
        }
        String destMuxName = "mux." + destination;
        MUX mux = NameRegistrar.getIfExists(destMuxName);

        if (isConnected(mux)) {
            try {
                ISOMsg request = context.get(REQUEST);
                if (request.hasField(62)&&request.hasField(63))
                    request.unset(62,63);
                ISOMsg response = mux.request(request, transactionTimeout);
                if (response == null) {
                    log.error("null response gotten from {}", destination);
                    request.setResponseMTI();
                    request.set(39, "99");//response code 99 means no response from the destination
                    context.put(RESPONSE, request);
                    context.put(DESTINATION_RESPONSE, "NO RESPONSE FROM DS");
                    return context;
                }

                if (response.hasField(52)) {
                    response.unset(52);
                }
                context.put(RESPONSE, response);
                context.put(DESTINATION_RESPONSE, response);
                return context;


            } catch (ISOException e) {
                e.printStackTrace();
                log.error("error sending transaction. error is {}", e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            log.error("destination is not connected");
            throw new DestinationNotConnectedException(destination + " is not connected");
        }
    }

    protected boolean isConnected(MUX mux) {
        if (mux.isConnected()) {
            return true;
        }
        final long timeout = System.currentTimeMillis() + 10000L;
        while (System.currentTimeMillis() < timeout) {
            if (mux.isConnected()) {
                return true;
            }
            ISOUtil.sleep(500L);
        }
        return false;
    }
}
