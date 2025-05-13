package com.vero.coreprocessor.ruleengine.dtos;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.io.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class BinResponseDto implements Serializable {
    private boolean status;
    private String message;
    private Data data;

    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class Data implements Serializable{
        private String bin;
        private String brand;
        @JsonProperty("sub_brand")
        private String subBrand;
        @JsonProperty("country_code")
        private String countryCode;
        @JsonProperty("country_name")
        private String countryName;
        @JsonProperty("card_type")
        private String cardType;
        private String bank;
        private String currency;
        @JsonProperty("linked_bank_id")
        private Integer linkedBankId;
    }
}
