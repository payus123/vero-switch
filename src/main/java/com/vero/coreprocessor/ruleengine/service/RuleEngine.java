package com.vero.coreprocessor.ruleengine.service;

import com.vero.coreprocessor.components.*;
import org.springframework.stereotype.*;

@Service
public interface RuleEngine {
    String getRoute(Context context);

}
