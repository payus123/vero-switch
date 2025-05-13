package com.vero.coreprocessor.transactions.model;

import com.vero.coreprocessor.merchants.model.Merchant;
import com.vero.coreprocessor.utils.BaseModel;
import jakarta.persistence.*;
import lombok.*;

import java.math.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "transaction")
@Getter
@Setter
public class Transaction  extends BaseModel {
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
    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;
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
    @Column(columnDefinition = "numeric")
    private BigDecimal charges = BigDecimal.ZERO;
    private BigDecimal destinationFee = BigDecimal.ZERO;
    private boolean isInternational;

}
