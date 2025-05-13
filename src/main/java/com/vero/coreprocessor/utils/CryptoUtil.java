// 
// Decompiled by Procyon v0.5.36
// 

package com.vero.coreprocessor.utils;

import org.bouncycastle.crypto.*;

public interface CryptoUtil
{
    String translatePin(final String pinblock, final String tpk, final String zpk);
    String decryptPinBlock(String pinBlock, String key) throws CryptoException;
}
