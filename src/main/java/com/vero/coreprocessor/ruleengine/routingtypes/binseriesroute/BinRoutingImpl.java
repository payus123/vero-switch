package com.vero.coreprocessor.ruleengine.routingtypes.binseriesroute;

import com.google.gson.*;
import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.ruleengine.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.springframework.stereotype.*;

@Service("BIN SERIES")
@Slf4j
@RequiredArgsConstructor
public class BinRoutingImpl implements RuleNameInterface, Constants {
    Gson gson = new Gson();
    @Override
    public boolean check(String condition, Context context) {
        log.info("doing bin series routing");
        BinSeriesRouteParam routeParam = gson.fromJson(condition, BinSeriesRouteParam.class);
        ISOMsg req = context.get(REQUEST);
        String[]binRange = routeParam.getRange().split("-");

        if (binRange[0]==null||binRange[1]==null){
            log.info("bin range must not be null");
            return false;
        }

        Long cardBin = Long.parseLong(req.getString(2).substring(0,6));
        Long lowerBound = Long.parseLong(binRange[0]);
        Long upperBound = Long.parseLong(binRange[1]);

        if (cardBin.compareTo(lowerBound)>0&&upperBound.compareTo(cardBin)>=0){
            log.info("bin {}} is in range {}",cardBin,routeParam.getRange());
            return true;
        }
        return false;
    }
}
