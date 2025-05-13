package com.vero.coreprocessor.ruleengine.service;

import com.vero.coreprocessor.bins.models.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class InternationalCardValidationService {
    private final  BinService binService;

    public boolean validateInternationalCard(String bin, String currencyCode) {
        Bin binData = binService.fetchBinDetails(bin);

        if (binData == null) {
            return false;
        }

        String cardCurrency = binData.getCurrency();
        log.info("International Card Currency: {}", binData);

        String currencyName = CURRENCY_MAP.getOrDefault(currencyCode.substring(1), "NGN");
        return !cardCurrency.equalsIgnoreCase(currencyName);
    }

    public static final Map<String, String> CURRENCY_MAP = Map.<String, String>ofEntries(
            Map.entry("008", "ALL"), // Albanian Lek
            Map.entry("012", "DZD"), // Algerian Dinar
            Map.entry("032", "ARS"), // Argentine Peso
            Map.entry("036", "AUD"), // Australian Dollar
            Map.entry("044", "BSD"), // Bahamian Dollar
            Map.entry("048", "BHD"), // Bahraini Dinar
            Map.entry("050", "BDT"), // Bangladeshi Taka
            Map.entry("056", "EUR"), // Euro (Belgium)
            Map.entry("096", "BND"), // Brunei Dollar
            Map.entry("156", "CNY"), // Chinese Yuan
            Map.entry("344", "HKD"), // Hong Kong Dollar
            Map.entry("398", "KZT"), // Kazakhstani Tenge
            Map.entry("566", "NGN"), // Nigerian Naira
            Map.entry("826", "GBP"), // British Pound Sterling
            Map.entry("840", "USD"), // US Dollar
            Map.entry("978", "EUR"), // Euro
            Map.entry("985", "PLN"), // Polish Zloty
            Map.entry("024", "AFN"), // Afghan Afghani
            Map.entry("072", "BBD"), // Barbadian Dollar
            Map.entry("104", "BRL"), // Brazilian Real
            Map.entry("124", "CAD"), // Canadian Dollar
            Map.entry("136", "BAM"), // Bosnia and Herzegovina Convertible Marka
            Map.entry("144", "CYP"), // Cypriot Pound
            Map.entry("152", "CLP"), // Chilean Peso
            Map.entry("170", "COP"), // Colombian Peso
            Map.entry("246", "EGP"), // Egyptian Pound
            Map.entry("250", "ERN"), // Eritrean Nakfa
            Map.entry("320", "FJD"), // Fijian Dollar
            Map.entry("328", "GHS"), // Ghanaian Cedi
            Map.entry("332", "GMD"), // Gambian Dalasi
            Map.entry("340", "GNF"), // Guinean Franc
            Map.entry("348", "GTQ"), // Guatemalan Quetzal
            Map.entry("356", "INR"), // Indian Rupee
            Map.entry("360", "IDR"), // Indonesian Rupiah
            Map.entry("376", "ILS"), // Israeli Shekel
            Map.entry("392", "JPY"), // Japanese Yen
            Map.entry("404", "KZT"), // Kazakhstani Tenge
            Map.entry("410", "KRW"), // South Korean Won
            Map.entry("414", "KWD"), // Kuwaiti Dinar
            Map.entry("498", "LKR"), // Sri Lankan Rupee
            Map.entry("516", "LBP"), // Lebanese Pound
            Map.entry("586", "MAD"), // Moroccan Dirham
            Map.entry("608", "MNT"), // Mongolian Tugrik
            Map.entry("616", "MDL"), // Moldovan Leu
            Map.entry("624", "MUR"), // Mauritian Rupee
            Map.entry("654", "MWK"), // Malawian Kwacha
            Map.entry("702", "NAD"), // Namibian Dollar
            Map.entry("710", "NPR"), // Nepalese Rupee
            Map.entry("748", "OMR"), // Omani Rial
            Map.entry("752", "NOK"), // Norwegian Krone
            Map.entry("764", "PKR"), // Pakistani Rupee
            Map.entry("784", "AED"), // United Arab Emirates Dirham
            Map.entry("800", "QAR"), // Qatari Riyal
            Map.entry("818", "RUB"), // Russian Ruble
            Map.entry("946", "SBD"), // Solomon Islands Dollar
            Map.entry("949", "SAR"), // Saudi Riyal
            Map.entry("951", "SIT"), // Slovenian Tolar
            Map.entry("960", "THB"), // Thai Baht
            Map.entry("972", "TZS"), // Tanzanian Shilling
            Map.entry("980", "UAH"), // Ukrainian Hryvnia
            Map.entry("984", "UGX"), // Ugandan Shilling
            Map.entry("988", "VND"), // Vietnamese Dong
            Map.entry("990", "YER"), // Yemeni Rial
            Map.entry("997", "ZWL")  // Zimbabwean Dollar
    );
}
