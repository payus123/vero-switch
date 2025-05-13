// 
// Decompiled by Procyon v0.5.36
// 

package com.vero.coreprocessor.utils;

import com.vero.coreprocessor.exceptions.*;
import org.apache.commons.codec.*;
import org.jpos.iso.*;

public interface MacUtils
{
    boolean assertMac(final ISOMsg req);
    
    ISOMsg computeMAC(final ISOMsg req) throws MacException, ISOException, DecoderException;
}
