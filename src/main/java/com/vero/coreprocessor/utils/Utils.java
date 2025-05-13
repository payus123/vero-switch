// 
// Decompiled by Procyon v0.5.36
// 

package com.vero.coreprocessor.utils;

import org.apache.commons.codec.*;
import org.apache.commons.codec.binary.*;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.*;
import org.jpos.iso.*;

import java.math.*;
import java.security.*;
import java.util.*;

public class Utils
{
    public static String getMac(final String seed, final byte[] macDataBytes) throws Exception {
        final byte[] keyBytes = h2b(seed);
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(keyBytes, 0, keyBytes.length);
        digest.update(macDataBytes, 0, macDataBytes.length);
        final byte[] hashedBytes = digest.digest();
        String hashText = b2h(hashedBytes);
        hashText = hashText.replace(" ", "");
        if (hashText.length() < 64) {
            final int numberOfZeroes = 64 - hashText.length();
            String zeroes = "";
            String temp = hashText;
            for (int i = 0; i < numberOfZeroes; ++i) {
                zeroes += "0";
            }
            temp = zeroes + temp;
            return temp;
        }
        return hashText;
    }
    
    public static byte[] h2b(final String hex) {
        if ((hex.length() & 0x1) == 0x1) {
            throw new IllegalArgumentException();
        }
        final byte[] bytes = new byte[hex.length() / 2];
        for (int idx = 0; idx < bytes.length; ++idx) {
            final int hi = Character.digit((int)hex.charAt(idx * 2), 16);
            final int lo = Character.digit((int)hex.charAt(idx * 2 + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException();
            }
            bytes[idx] = (byte)(hi << 4 | lo);
        }
        return bytes;
    }
    
    public static String b2h(final byte[] bytes) {
        final char[] hex = new char[bytes.length * 2];
        for (int idx = 0; idx < bytes.length; ++idx) {
            final int hi = (bytes[idx] & 0xF0) >>> 4;
            final int lo = bytes[idx] & 0xF;
            hex[idx * 2] = (char)((hi < 10) ? (48 + hi) : (55 + hi));
            hex[idx * 2 + 1] = (char)((lo < 10) ? (48 + lo) : (55 + lo));
        }
        return new String(hex);
    }
    
    public static String leftPad(final String str, final int len, final char pad) {
        if (str == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        while (sb.length() + str.length() < len) {
            sb.append(pad);
        }
        sb.append(str);
        final String paddedString = sb.toString();
        return paddedString;
    }
    
    public static String rightPad(final String str, final int len, final char pad) {
        if (str == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(str);
        while (sb.length() < len) {
            sb.append(pad);
        }
        final String paddedString = sb.toString();
        return paddedString;
    }
    
    public static byte[] generateHash256Value(final byte[] iso, final byte[] key) throws DecoderException {
        String hashText = null;
        try {
            final MessageDigest m = MessageDigest.getInstance("SHA-256");
            m.update(key, 0, key.length);
            m.update(iso, 0, iso.length);
            hashText = b2h(m.digest());
            hashText = hashText.replace(" ", "");
        }
        catch (NoSuchAlgorithmException ex) {
            System.out.println("Hashing ");
        }
        if (hashText.length() < 64) {
            final int numberOfZeroes = 64 - hashText.length();
            String zeroes = "";
            String temp = hashText;
            for (int i = 0; i < numberOfZeroes; ++i) {
                zeroes += "0";
            }
            temp = zeroes + temp;
            System.out.println("Utility :: generateHash256Value :: HashValue with zeroes: {}" + temp);
            return Hex.decodeHex(temp);
        }
        return Hex.decodeHex(hashText);
    }
    //{Integer@14558} 64 -> {ISOBinaryField@14664} "0B9FE11702C3836849B0099F656EF0A33E81BA8D43D512EEE05949ED5A8C84E9"
    public static boolean validateMAC(final ISOMsg msg, final String SessionKey) throws ISOException, DecoderException {
        final String requestfield128 = !Objects.isNull(msg.getString(64))?msg.getString(64):msg.getString(128);
        final byte[] sessionKeyBytes = h2b(SessionKey);
        final byte[] bites = msg.pack();
        final int length = bites.length;
        final byte[] temp = new byte[length - 64];
        if (length >= 64) {
            System.arraycopy(bites, 0, temp, 0, length - 64);
        }
        final String correctField128 = org.bouncycastle.util.encoders.Hex.toHexString(generateHash256Value(temp, sessionKeyBytes));
        System.out.println("the full message is=====: "+ ISOUtil.byte2hex(bites));
        System.out.println("Field 128: "+correctField128);
        return Objects.equals(requestfield128.toUpperCase(), correctField128.toUpperCase());
    }
    
    public static String decryptKey(final String encryptedKey, final String key) throws CryptoException, DecoderException {
        try {
            final byte[] tmsKeyBytes = Hex.decodeHex(key.toCharArray());
            final byte[] pinBlockBytes = Hex.decodeHex(encryptedKey.toCharArray());
            final byte[] clearPinBlockBytes = EncryptionUtils.tdesDecryptECB(pinBlockBytes, tmsKeyBytes);
            return new String(Hex.encodeHex(clearPinBlockBytes));
        }
        catch (org.bouncycastle.util.encoders.DecoderException e) {
            throw new CryptoException("Could not decode hex key", e);
        }
    }
    
    public String encryptPinBlock(final String pinBlock, final String key) throws CryptoException, DecoderException {
        if (StringUtils.isEmpty(pinBlock)) {
            return pinBlock;
        }
        byte[] clearPinBlockBytes;
        byte[] zpk;
        try {
            clearPinBlockBytes = Hex.decodeHex(pinBlock.toCharArray());
            zpk = Hex.decodeHex(key.toCharArray());
        }
        catch (org.bouncycastle.util.encoders.DecoderException e) {
            throw new CryptoException("Could not decode pin block for Threeline", e);
        }
        final byte[] encryptedPinBlockBytes = EncryptionUtils.tdesEncryptECB(clearPinBlockBytes, zpk);
        return new String(Hex.encodeHex(encryptedPinBlockBytes));
    }
    
    public static byte[] hexStringToByteArray(final String s) {
        final int len = s.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    public static String generateMAC(final ISOMsg msg, final byte[] sessionKeyBytes) throws ISOException {
        final byte[] bites = msg.pack();
        final int length = bites.length;
        final byte[] temp = new byte[length - 64];
        if (length >= 64) {
            System.arraycopy(bites, 0, temp, 0, length - 64);
        }
        return generateHash256ValueString(temp, sessionKeyBytes);
    }
    
    private static String generateHash256ValueString(final byte[] iso, final byte[] key) {
        String hashText = null;
        try {
            final MessageDigest m = MessageDigest.getInstance("SHA-256");
            m.update(key, 0, key.length);
            m.update(iso, 0, iso.length);
            hashText = ISOUtil.byte2hex(m.digest());
            hashText = hashText.replace(" ", "");
        }
        catch (NoSuchAlgorithmException ex) {
            System.out.println("Hashing ");
        }
        if (hashText.length() < 64) {
            final int numberOfZeroes = 64 - hashText.length();
            String zeroes = "";
            String temp = hashText;
            for (int i = 0; i < numberOfZeroes; ++i) {
                zeroes += "0";
            }
            temp = zeroes + temp;
            System.out.println("Utility :: generateHash256Value :: HashValue with zeroes: {}" + temp);
            return temp;
        }
        return hashText;
    }


    public static ISOMsg getIsoMsgInstance(ISOMsg msg){
        ISOMsg isoMsg = (ISOMsg) msg.clone();
        return isoMsg ;
    }

    public static Map<String,String> parseField126(String de126) {
        Map<String, String> tlvList = new LinkedHashMap<>();
        String temp = de126;
        while (temp.length() > 0) {
            String tag = temp.substring(0, 2);
            temp = temp.substring(2);
            int length = Integer.parseInt(temp.substring(0, 3));
            temp = temp.substring(3);
            String value = temp.substring(0, length);
            temp = temp.substring(length);
            tlvList.put(tag, value);
        }
        return  tlvList;
    }

    public static BigDecimal convertIsoAmountToLegacy(String isoAmount) {
        if (isoAmount == null || isoAmount.length() != 12) {
            throw new IllegalArgumentException("Invalid input: ISO amount must be a 12-digit numeric string");
        }

        // Convert the ISO amount field to a BigDecimal by stripping left-padded zeros
        BigDecimal amountInSmallestUnit = new BigDecimal(isoAmount);

        // Divide by 100 with rounding mode to prevent ArithmeticException
        return amountInSmallestUnit.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

}
