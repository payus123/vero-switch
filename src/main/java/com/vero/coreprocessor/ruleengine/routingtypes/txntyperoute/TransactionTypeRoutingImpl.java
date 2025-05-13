package com.vero.coreprocessor.ruleengine.routingtypes.txntyperoute;

import com.google.gson.*;
import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.ruleengine.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionTypeRoutingImpl implements RuleNameInterface, Constants {
    static ConcurrentHashMap<String,String> map = new ConcurrentHashMap<>();
    Gson gson = new Gson();

    @Override
    public boolean check(String condition, Context context) {
        TxnTypeRouteParam routeParam = gson.fromJson(condition, TxnTypeRouteParam.class);
        String transactionType = context.getString(TXNNAME);
        String transactionName = map.get(transactionType);

        if (transactionName.equals(routeParam.getTransactionType())){
            log.info("transaction type route gotten {}",transactionName);
            return true;
        }

        return false;
    }

    static {
        map.put ("100.31", "BALANCE_INQUIRY");
        map.put ("100.60", "PRE_AUTH");
        map.put ("200.00", "PURCHASE");
        map.put ("200.01", "CASH_ADVANCE");
        map.put ("200.09", "CASHBACK");
        map.put ("200.20", "REFUND");

        map.put ("420.00", "REVERSAL");
        map.put ("420.20", "REFUND_REVERSAL");
        map.put ("421.00", "COMPLETION_REVERSAL");
        map.put ("420.61", "PREAUTH_REVERSAL");

    }
}
