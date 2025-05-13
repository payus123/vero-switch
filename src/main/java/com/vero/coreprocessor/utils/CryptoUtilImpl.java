// 
// Decompiled by Procyon v0.5.36
// 

package com.vero.coreprocessor.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.*;
import org.bouncycastle.util.encoders.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

@Service
public class CryptoUtilImpl implements CryptoUtil {
    private final Logger logger = LoggerFactory.getLogger(CryptoUtilImpl.class);
    @Override
    public String translatePin(String pinblock, String tpk, String zpk) {
        String translatedPin = "";

        try {
            translatedPin = decryptPinBlock(pinblock,tpk);
            System.out.println("Clear pinBlock is===========  "+translatedPin);
            translatedPin = encryptPinBlock(translatedPin,zpk);
        } catch (CryptoException e) {
            e.printStackTrace();
            logger.info(e.getLocalizedMessage());
        }

        return translatedPin;
    }

//   @PostConstruct
//   public void testThis() throws CryptoException {
//       String s = decryptPinBlock("0C540CB5C8997DDFCC345DD40BB68A97", "87a935fafa428dc309d43596e89f1dff");
//       System.out.println("this is the key==========================:  "+s);
//   }

    public String decryptPinBlock(String pinBlock, String key) throws CryptoException {
        try {
            byte[] tmsKeyBytes = Hex.decodeHex(key.toCharArray());
            byte[] pinBlockBytes = Hex.decodeHex(pinBlock.toCharArray());

            byte[] clearPinBlockBytes = EncryptionUtils.tdesDecryptECB(pinBlockBytes, tmsKeyBytes);

            return new String(Hex.encodeHex(clearPinBlockBytes));
        } catch (DecoderException | org.apache.commons.codec.DecoderException e) {
            throw new CryptoException("Could not decode hex key", e);
        }
    }

    public String encryptPinBlock(String pinBlock, String key) throws CryptoException {
        logger.info("The pin block bytes {} ", pinBlock);
        if (StringUtils.isEmpty(pinBlock)) {
            return pinBlock;
        }
        byte[] clearPinBlockBytes;
        byte[] zpk;
        try {
            clearPinBlockBytes = Hex.decodeHex(pinBlock.toCharArray());
            zpk = Hex.decodeHex(key.toCharArray());
        } catch (DecoderException | org.apache.commons.codec.DecoderException e) {
            throw new CryptoException("Could not decode pin block for Threeline", e);
        }
        byte[] encryptedPinBlockBytes = EncryptionUtils.tdesEncryptECB(clearPinBlockBytes, zpk);
        return new String(Hex.encodeHex(encryptedPinBlockBytes));

    }
}
