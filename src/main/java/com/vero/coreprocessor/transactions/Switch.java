package com.vero.coreprocessor.transactions;

import com.vero.coreprocessor.constants.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.vero.coreprocessor.components.Context;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service @Slf4j @RequiredArgsConstructor
public class Switch implements Constants {
    private final ApplicationContext applicationContext;
    public Context switchTransactionType(Context context){

        String transactionName = context.getString(TXNNAME);
        TransactionTypeSwitch transactionTypeSwitch= applicationContext.getBean(transactionName, TransactionTypeSwitch.class);

        return transactionTypeSwitch.process(context);
    }
}
