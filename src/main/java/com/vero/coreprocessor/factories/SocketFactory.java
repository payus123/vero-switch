package com.vero.coreprocessor.factories;

import org.jpos.iso.*;

public class SocketFactory extends GenericSSLSocketFactory {

    @Override
    public void setKeyStore(String keyStore) {
        super.setKeyStore(keyStore);
    }

    @Override
    protected String getKeyPassword() {
        return "jposjpos";
    }
}
