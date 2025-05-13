package com.vero.coreprocessor.ruleengine.routingtypes.schemeroute;

import com.google.gson.*;
import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.ruleengine.service.*;
import com.vero.coreprocessor.scheme.model.*;
import com.vero.coreprocessor.scheme.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.springframework.stereotype.*;

import java.math.*;
import java.util.*;
import java.util.regex.*;

@Service("SCHEME")
@Slf4j
@RequiredArgsConstructor
public class SchemeRoutingImpl implements RuleNameInterface, Constants {
    private final SchemeService schemeService;
    Gson gson = new Gson();
    @Override
    public boolean check(String condition, Context context) {
        SchemeRouteParam routeParam = gson.fromJson(condition, SchemeRouteParam.class);
        
        List<Scheme> allSchemes = schemeService.getAllSchemes();
        ISOMsg req = context.get(REQUEST);
        String schemeName = "";
        for (Scheme scheme:allSchemes){
            boolean matched = Pattern.compile(scheme.getRegex()).matcher(req.getString(2)).matches();
            if (matched){
                log.info("scheme gotten is {}",scheme.getSchemeName());
                schemeName = scheme.getSchemeName();
                break;
            }
        }
        if (schemeName.equalsIgnoreCase("")){
            log.info("scheme not found for card {}", ISOUtil.protect(req.getString(2),'*'));
            return false;
        }

        if (schemeName.equalsIgnoreCase(routeParam.getSchemeName())){
            //check for amount
            BigDecimal transactionAmount = getTransactionAmount(req.getString(4));
            BigDecimal minimumAmount = new BigDecimal(routeParam.getMinimumAmount());
            BigDecimal maximumAmount = new BigDecimal(routeParam.getMaximumAmount());
            return transactionAmount.compareTo(minimumAmount) > 0 && transactionAmount.compareTo(maximumAmount) <= 0;
        }
        return false;
    }


    private BigDecimal getTransactionAmount(String amount){
        return new BigDecimal(amount).movePointLeft(2);
    }
}
