package com.vero.coreprocessor.ruleengine.service;

import com.vero.coreprocessor.components.*;

public interface RuleNameInterface {
    boolean check(String condition, Context context);
}
