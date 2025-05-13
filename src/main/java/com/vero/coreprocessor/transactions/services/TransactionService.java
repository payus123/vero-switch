package com.vero.coreprocessor.transactions.services;

import com.vero.coreprocessor.ruleengine.dtos.*;
import com.vero.coreprocessor.transactions.model.Transaction;
import org.jpos.iso.ISOMsg;
import com.vero.coreprocessor.components.Context;

public interface TransactionService {
    Transaction saveTransaction(Context context, ISOMsg req);
    void postProcess(Transaction transaction);
    boolean checkDuplicateTransaction(ISOMsg request);
    Transaction findOriginalTransaction(String stan, String mti, String dateTime);
    void calculateFee(Context context, PreTransactionInfo preTransactionInfo, String destination);


}
