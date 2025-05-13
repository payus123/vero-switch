package com.vero.coreprocessor.keymodule.service;

import com.vero.coreprocessor.components.*;

public interface KeyExchangeService {
    Context doMasterKey(Context request);
    Context doSessionKey(Context request);
    Context doPinKey(Context request);
    Context downloadParameter(Context request);
}
