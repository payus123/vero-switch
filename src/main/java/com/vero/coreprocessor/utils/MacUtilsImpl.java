// 
// Decompiled by Procyon v0.5.36
// 

package com.vero.coreprocessor.utils;

import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.keymodule.model.*;
import com.vero.coreprocessor.keymodule.repository.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.apache.commons.codec.*;
import org.apache.commons.codec.binary.*;
import org.jpos.iso.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MacUtilsImpl implements MacUtils {
    private final TerminalKeysRepository terminalKeysRepository;

    @Value("${sessionKey}")
    String hostSessionKey;

    @Override
    public boolean assertMac(final ISOMsg req) {
        try {
            TerminalKeys terminalKeys = terminalKeysRepository.findByTerminalId(req.getString(41)).orElseThrow(() -> {
                throw new MacException("Terminal Keys not found");
            });


            final String sessionKey = terminalKeys.getTsk().substring(0, 32);
            if (sessionKey.isEmpty()) {
                MacUtilsImpl.log.info("Session Key not found, recommend key Exchange");
                throw new MacException("Session Key not found to be used to validate MAC");
            }
            boolean result = Utils.validateMAC(req, sessionKey);
            if (!result) {
                log.error("mac value is wrong, please check");
            }
            return true;
        } catch (Exception e) {
            log.error("Unable to validate mac");
            throw new MacException("Unable to validate mac:  "+ e.getMessage());
        }


    }

    @Override
    public ISOMsg computeMAC(final ISOMsg msg) throws MacException, ISOException, DecoderException {
        if (msg == null) {
            MacUtilsImpl.log.error("keys not present or empty.");
            throw new MacException("Keys not present or empty");
        }

        final String sessionKey = hostSessionKey;
        if (sessionKey.isEmpty()) {
            MacUtilsImpl.log.info("Session Key not found, recommend key Exchange");
            throw new MacException("Session Key not found to be used to validate MAC");
        }
        String hmac = "";
        if (msg.hasField(64)) {
            MacUtilsImpl.log.info("Field 64 found");
            hmac = Utils.generateMAC(msg, Utils.hexStringToByteArray(sessionKey));
            msg.set(64, Hex.decodeHex(hmac));
        } else if (msg.hasField(128)) {
            MacUtilsImpl.log.info("Field 128 found");
            hmac = Utils.generateMAC(msg, Utils.hexStringToByteArray(sessionKey));
            msg.set(128, Hex.decodeHex(hmac));
        } else {
            msg.unset(3);
            MacUtilsImpl.log.info("HMAC calculation skipped.");
        }
        if (msg.hasField(127)) {
            msg.unset(127);
        }
        return msg;
    }

}
