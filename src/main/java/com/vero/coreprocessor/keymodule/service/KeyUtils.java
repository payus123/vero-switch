package com.vero.coreprocessor.keymodule.service;

import org.jpos.security.*;

public interface KeyUtils {
    String generateKey(short keyLength);
    SecureDESKey importKey(short keyLength, String keyType, byte[] encryptedKey,
                           SecureDESKey kek, boolean checkParity);
    SecureDESKey exportKey(SecureDESKey key, SecureDESKey kek);

    String translatePIN(String ePin, String tpk, String zpk);

    String  formKeyFromThreeComponents(short keyLength,String clearComponent1HexString, String clearComponent2HexString, String clearComponent3HexString);


}
