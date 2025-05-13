package com.vero.coreprocessor.ruleengine.routingtypes.terminalprefixroute;

import com.google.gson.*;
import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.ruleengine.service.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.springframework.stereotype.*;

@Service
@Slf4j
public class TerminalPrefixImpl implements RuleNameInterface, Constants {
    Gson gson = new Gson();

    @Override
    public boolean check(String condition, Context context) {
        TerminalPrefixRouteParam routeParam = gson.fromJson(condition, TerminalPrefixRouteParam.class);
        log.info("starting terminal prefix routing");
        ISOMsg req =context.get(REQUEST);
        String terminalID = req.getString(41);
        if (terminalID.startsWith(routeParam.getPrefix())){
            log.info("terminal prefix rule matched");
            return true;
        }
        return false;
    }
}
