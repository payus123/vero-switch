package com.vero.coreprocessor.transactions.enums;

import java.util.HashMap;
import java.util.Map;

public enum TransactionType {
    PURCHASE("200.00"),
    CASH_ADVANCE("200.01"),
    REVERSAL("420.00"),
    REFUND("200.20"),
    DEPOSIT("200.21"),
    PURCHASE_WITH_CASHBACK("0200.09"),
    BALANCE_INQUIRY("0100.31"),
    LINK_ACCOUNT_INQUIRY("0100.30"),
    MINI_STATEMENT("0100.38"),
    FUND_TRANSFER("0200.40"),
    BILL_PAYMENT("0200.48"),
    PREPAID("0200.4A"),
    PURCHASE_WITH_ADDITIONAL_DATA("0200.4F"),
    PRE_AUTH("0100.60"),
    PRE_AUTH_COMPLETION("0220.61"),
    PIN_CHANGE("0100.90");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static final Map<String, TransactionType> LOOKUP_MAP = new HashMap<>();

    static {
        for (TransactionType type : TransactionType.values()) {
            LOOKUP_MAP.put(type.value.toLowerCase(), type);
        }
    }

    public static TransactionType fromString(String text) {
        TransactionType type = LOOKUP_MAP.get(text.toLowerCase());
        if (type == null) {
            throw new IllegalArgumentException("No matching TransactionType for value: " + text);
        }
        return type;
    }
}
