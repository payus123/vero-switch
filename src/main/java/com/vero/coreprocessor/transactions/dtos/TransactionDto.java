package com.vero.coreprocessor.transactions.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.*;

@Data
@JsonSerialize
public class TransactionDto {
    private String mti;
    private String maskedPan;
    private String procCode;
    private String transactionAmount;
    private String tranDateAndTime;
    private String stan;
    private String tranTime;
    private String tranDate;
    private String cardExpDate;
    private String settlementDate;
    private String merchantCategoryCode;
    private String posEntryMode;
    private String cardSeqNumber;
    private String posConditionCode;
    private String transactionFee;
    private String processingFee;
    private String acquiringInstitutionID;
    private String forwardingInstitutionID;
    private String track2;
    private String rrn;
    private String authorizationID;
    private String responseCode;
    private String serviceRestrictionCode;
    private String terminalID;
    private String merchantID;
    private double charges;
    private String institutionId;
    private String merchantUniqueReference;
    private String merchantNameAndLocation;
    private String currencyCode;
    private String additionalAmounts;
    private String accountID1;
    private String accountID2;
    private String posDataCode;
    private String destination;
    private String switchTid;
    private boolean isInternational;;
    private BigDecimal destinationFee = BigDecimal.ZERO;;

}
