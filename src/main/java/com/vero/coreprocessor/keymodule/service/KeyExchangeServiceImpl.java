package com.vero.coreprocessor.keymodule.service;

import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.keymodule.model.*;
import com.vero.coreprocessor.keymodule.repository.*;
import com.vero.coreprocessor.merchants.model.*;
import com.vero.coreprocessor.terminals.model.*;
import com.vero.coreprocessor.utils.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.bouncycastle.crypto.*;
import org.jpos.iso.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

import java.text.*;
import java.util.*;

import static com.vero.coreprocessor.utils.EncryptionUtils.*;

@Service @RequiredArgsConstructor @Slf4j
public class KeyExchangeServiceImpl implements KeyExchangeService, Constants {
    private final KeyUtils keyUtils;
    @Value("${ctmk.component1}")
    private String component1;
    @Value("${ctmk.component2}")
    private String component2;

    private final TerminalKeysRepository terminalKeysRepository;

    @Override
    public Context doMasterKey(Context context) {
        ISOMsg request = Utils.getIsoMsgInstance(context.get(REQUEST));
        log.info("attempting masterKey download for terminal ID {}",request.getString(41));
        try {
            if (request.getString(3).startsWith("9A")){
                String masterKey = keyUtils.generateKey((short) 128);
                String sessionKey = keyUtils.generateKey((short) 128);
                String pinKey = keyUtils.generateKey((short) 128);

                TerminalKeys terminalKeys = terminalKeysRepository.findByTerminalId(request.getString(41)).orElse(new TerminalKeys());

                terminalKeys.setTerminalId(request.getString(41));
                terminalKeys.setTmk(masterKey.substring(0,32));
                terminalKeys.setTmkKcv(masterKey.substring(32));
                terminalKeys.setTsk(sessionKey.substring(0,32));
                terminalKeys.setTskKcv(sessionKey.substring(32));
                terminalKeys.setTpk(pinKey.substring(0,32));
                terminalKeys.setTpkKcv(pinKey.substring(32));
                terminalKeysRepository.save(terminalKeys);

                byte[] tmkUnderCtmk = tdesEncryptECB(ISOUtil.hex2byte(terminalKeys.getTmk()), getCTMKFromComponents(component1,component2));
                request.set(53,buildField53(tmkUnderCtmk ,ISOUtil.hex2byte(terminalKeys.getTmkKcv())));
                request.set(39,"00");


            }
            else {
                log.error("processing code is not a master key request");
                request.set(39,"96");
            }

        } catch (ISOException | CryptoException e) {
            request.set(39,"96");
            e.printStackTrace();
            log.error("error occurred in sessionKey, error is {}",e.getMessage());
        }

        context.put(RESPONSE,request);
        return context;
    }

    @Override
    public Context doSessionKey(Context context) {
        ISOMsg request = Utils.getIsoMsgInstance(context.get(REQUEST));
        log.info("attempting sessionKey download for terminal ID {}",request.getString(41));
        try {
            if (request.getString(3).startsWith("9B")){

                TerminalKeys terminalKeys = terminalKeysRepository.findByTerminalId(request.getString(41)).orElse(null);

                if (terminalKeys==null){
                    log.info("terminal exists error. attempt master key");
                    throw new EntityNotFoundException("terminal not found in terminal Keys table");
                }

                byte[] tskUnderTmk = tdesEncryptECB(ISOUtil.hex2byte(terminalKeys.getTsk()), ISOUtil.hex2byte(terminalKeys.getTmk()));
                request.set(53,buildField53(tskUnderTmk ,ISOUtil.hex2byte(terminalKeys.getTskKcv())));
                request.set(39,"00");


            }
            else {
                log.error("processing code is not a master key request");
                request.set(39,"96");
            }

        } catch (ISOException | CryptoException e) {
            request.set(39,"96");
            e.printStackTrace();
            log.error("error occurred in sessionKey, error is {}",e.getMessage());
        }
        context.put(RESPONSE,request);
        return context;

    }

    @Override
    public Context doPinKey(Context context) {
        ISOMsg request = Utils.getIsoMsgInstance(context.get(REQUEST));
        log.info("attempting pinKey download for terminal ID {}",request.getString(41));

        try {
            if (request.getString(3).startsWith("9G")){

                TerminalKeys terminalKeys = terminalKeysRepository.findByTerminalId(request.getString(41)).orElse(null);

                if (terminalKeys==null){
                    log.info("terminal exists error. attempt master key");
                    throw new EntityNotFoundException("terminal not found in terminal Keys table");
                }

                byte[] tskUnderTmk = tdesEncryptECB(ISOUtil.hex2byte(terminalKeys.getTpk()), ISOUtil.hex2byte(terminalKeys.getTmk()));
                request.set(53,buildField53(tskUnderTmk ,ISOUtil.hex2byte(terminalKeys.getTpkKcv())));
                request.set(39,"00");


            }
            else {
                log.error("processing code is not a pin key request");
                request.set(39,"96");
            }

        } catch (ISOException | CryptoException e) {
            request.set(39,"96");
            e.printStackTrace();
            log.error("error occurred in pinKey, error is {}",e.getMessage());
        }

        context.put(RESPONSE,request);
        return context;
    }

    @Override
    public Context downloadParameter(Context context) {

        Terminal terminal =context.get(TERMINAL);
        Merchant merchant = terminal.getMerchant();
        String merchantNameAndLocation = merchant.getMerchantNameAndLocation();
        ISOMsg request = Utils.getIsoMsgInstance(context.get(REQUEST));
        log.info("attempting parameter download ");

        try {
            String postParameters = "02014" + getTimeZoneDate()
                    + "03" + padLeftZeros(merchant.getMerchantId().length()) + merchant.getMerchantId()
                    + "04" + padLeftZeros("60".length()) + "60"
                    + "05" + padLeftZeros(merchant.getCurrencyCode().length()) + merchant.getCurrencyCode()
                    + "06" + padLeftZeros(merchant.getCountryCode().length()) + merchant.getCountryCode()
                    + "07" + padLeftZeros("60".length()) + "60"
                    + "52" + padLeftZeros(merchantNameAndLocation.length()) + merchantNameAndLocation
                    + "08" + padLeftZeros(merchant.getMerchantCategoryCode().length()) + merchant.getMerchantCategoryCode();
            if (request.hasField(62))
                postParameters = request.getString(62) + postParameters;
            request.set(62, postParameters);
            request.set(39, "00");
        }catch (Exception e){
            request.set(39,"96");
            log.error("error occurred in parameter download, error is {}",e.getMessage());
            e.printStackTrace();

        }
        context.put(RESPONSE,request);
        return context;


    }


    private byte[] buildField53(byte[] key, byte[] kcv) throws ISOException {
        return ISOUtil.hex2byte(ISOUtil.padright(ISOUtil.byte2hex(key) + ISOUtil.byte2hex(kcv).substring(0, 6), 96, '0'));
       // return ISOUtil.hex2byte(ISOUtil.padright(ISOUtil.byte2hex(key), 96, '0'));

    }

    private String getTimeZoneDate(){
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        //set timezone here
        dateFormat.setTimeZone(TimeZone.getTimeZone("Africa/Lagos"));

        return dateFormat.format(date);
    }

    private String padLeftZeros(final int input) {
        int length = 3;
        String inputString = String.valueOf(input);
        if (inputString.length() >= length) {
            return inputString;
        }
        final StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);
        return sb.toString();
    }

    private byte[]getCTMKFromComponents(String component1, String component2){
        return ISOUtil.xor(ISOUtil.hex2byte(component1),ISOUtil.hex2byte(component2));
    }


    public static void main(String[] args) {

    }


}
