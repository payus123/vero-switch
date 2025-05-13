package com.vero.coreprocessor.keymodule.service;

import lombok.extern.slf4j.*;
import org.apache.commons.codec.*;
import org.apache.commons.codec.binary.*;
import org.bouncycastle.crypto.*;
import org.jpos.iso.*;
import org.jpos.security.*;
import org.jpos.security.jceadapter.*;
import org.springframework.stereotype.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.*;

import static com.vero.coreprocessor.utils.EncryptionUtils.*;

@Service
@Slf4j
public class SSM implements KeyUtils {
    static final String ALG_DES = "DES";
    static final String ALG_TRIPLE_DES = "DESede";
    static final String DES_MODE_ECB = "ECB";
    static final String DES_MODE_CBC = "CBC";
    static final String DES_NO_PADDING = "NoPadding";
    /***
     * this method generates keys for ZMK and ZPK usage. generated
     * key is key length + key check value
     ***/
    @Override
    public String generateKey(short keyLength) {
        Key generatedClearKey;
        String clearKeyComponenetHexString;
        try {
            KeyGenerator keyGenerator;
            if (keyLength> SMAdapter.LENGTH_DES)
                keyGenerator = KeyGenerator.getInstance(ALG_TRIPLE_DES);
            else
                keyGenerator = KeyGenerator.getInstance(ALG_DES);

            generatedClearKey = keyGenerator.generateKey();

            byte[]clearKeyBytes = extractDESKeyMaterial(keyLength,generatedClearKey);
            Util.adjustDESParity(clearKeyBytes);
            generatedClearKey = formDESKey(keyLength,clearKeyBytes);

            byte[]clearKeyData = extractDESKeyMaterial(keyLength,generatedClearKey);
            clearKeyComponenetHexString = ISOUtil.hexString(clearKeyData);

            //generate kcv for key
            byte[] keyCheckValue = generateKeyCheckValue(clearKeyData);

            clearKeyComponenetHexString = clearKeyComponenetHexString + ISOUtil.byte2hex(keyCheckValue).toUpperCase();

        } catch (NoSuchAlgorithmException | JCEHandlerException | CryptoException e) {
            log.error("error generating key {}",e.getMessage());
            throw new RuntimeException(e);
        }

        return clearKeyComponenetHexString;
    }

    @Override
    public SecureDESKey importKey(short keyLength, String keyType, byte[] encryptedKey, SecureDESKey kek, boolean checkParity) {
        return null;
    }

    @Override
    public SecureDESKey exportKey(SecureDESKey key, SecureDESKey kek) {
        return null;
    }

    @Override
    public String translatePIN(String ePin, String tpk, String zpk) {
        try {
            byte[] clearPinBlock = tdesDecryptECB(Hex.decodeHex(ePin.toCharArray()), Hex.decodeHex(tpk.toCharArray()));
            byte[] translatedPin = tdesEncryptECB(clearPinBlock, Hex.decodeHex(zpk.toCharArray()));

            return ISOUtil.byte2hex(translatedPin);
        } catch (CryptoException | DecoderException e) {
            log.error("error translating pin. error is {}",e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    @Override
    public String formKeyFromThreeComponents(short keyLength, String clearComponent1HexString, String clearComponent2HexString, String clearComponent3HexString) {
        String clearKeyComponenetHexString = "";
        String result = "";
        try {
            byte[] clearComponent1 = ISOUtil.hex2byte(clearComponent1HexString);
            byte[] clearComponent2 = ISOUtil.hex2byte(clearComponent2HexString);
            byte[] clearKeyBytes = ISOUtil.xor(clearComponent1, clearComponent2);

            if (!Objects.isNull(clearComponent3HexString)) {
                byte[] component3 = ISOUtil.hex2byte(clearComponent2HexString);
                clearKeyBytes = ISOUtil.xor(clearKeyBytes, component3);
            }


            Key clearKey = null;
            clearKey = formDESKey(keyLength, clearKeyBytes);
            byte[]clearKeyData = extractDESKeyMaterial(keyLength,clearKey);
            clearKeyComponenetHexString = ISOUtil.hexString(clearKeyData);

            byte[] keyCheckValue = generateKeyCheckValue(ISOUtil.hex2byte(clearKeyComponenetHexString));
            result = clearKeyComponenetHexString+ISOUtil.hexString(keyCheckValue);

        } catch (JCEHandlerException | CryptoException e) {
            log.error("error combining key components");
        }


        return result;
    }

    protected byte[] extractDESKeyMaterial(short keyLength, Key clearDESKey) throws JCEHandlerException {
        String keyAlg = clearDESKey.getAlgorithm();
        String keyFormat = clearDESKey.getFormat();
        if (keyFormat.compareTo("RAW") != 0) {
            throw new JCEHandlerException("Unsupported DES key encoding format: " + keyFormat);
        }
        if (!keyAlg.startsWith(ALG_DES)) {
            throw new JCEHandlerException("Unsupported key algorithm: " + keyAlg);
        }
        byte[] clearKeyBytes = clearDESKey.getEncoded();
        clearKeyBytes = ISOUtil.trim(clearKeyBytes, getBytesLength(keyLength));
        return clearKeyBytes;
    }

    int getBytesLength(short keyLength) throws JCEHandlerException {
        return switch (keyLength) {
            case SMAdapter.LENGTH_DES -> 8;
            case SMAdapter.LENGTH_DES3_2KEY -> 16;
            case SMAdapter.LENGTH_DES3_3KEY -> 24;
            default -> throw new JCEHandlerException("Unsupported key length: " + keyLength + " bits");
        };
    }

    protected Key formDESKey(short keyLength, byte[] clearKeyBytes) throws JCEHandlerException {
        Key key = null;
        switch (keyLength) {
            case SMAdapter.LENGTH_DES: {
                key = new SecretKeySpec(clearKeyBytes, ALG_DES);
            }
            break;
            case SMAdapter.LENGTH_DES3_2KEY: {
                // make it 3 components to work with JCE
                clearKeyBytes = ISOUtil.concat(clearKeyBytes, 0, getBytesLength(SMAdapter.LENGTH_DES3_2KEY), clearKeyBytes, 0,
                        getBytesLength(SMAdapter.LENGTH_DES));
            }
            case SMAdapter.LENGTH_DES3_3KEY: {
                key = new SecretKeySpec(clearKeyBytes, ALG_TRIPLE_DES);
            }
        }
        if (key == null)
            throw new JCEHandlerException("Unsupported DES key length: " + keyLength + " bits");
        return key;
    }

    public static byte[] generateKeyCheckValue(byte[] key) throws CryptoException {
        byte[] zeroBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] fullKeyCheck = tdesEncryptECB(zeroBytes, key);
        byte[] keyCheckValue = new byte[3];
        System.arraycopy(fullKeyCheck, 0, keyCheckValue, 0, keyCheckValue.length);

        return keyCheckValue;
    }

}
