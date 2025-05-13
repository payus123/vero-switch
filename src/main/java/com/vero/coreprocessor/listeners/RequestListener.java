// 
// Decompiled by Procyon v0.5.36
// 

package com.vero.coreprocessor.listeners;

import org.jpos.iso.*;
import org.jpos.util.*;

public class RequestListener extends Log implements ISORequestListener {

    @Override
    public boolean process(ISOSource source, ISOMsg reqMsg) {

        try {

            if (reqMsg.isResponse())
                return false;

            if (reqMsg.getMTI().equals("0800")) {

                info("Network Management request received.");

                ISOMsg logonMsg = (ISOMsg) reqMsg.clone();

                logonMsg.setResponseMTI();
                logonMsg.set(39, "00");

                source.send(logonMsg);

                info("Network management response sent.");

                return true;
            }
            else {

                warn("Unsupported request received from Host: " + reqMsg.getMTI());
            }
        }
        catch (Exception e) {
            error(e);
        }

        return false;
    }
}