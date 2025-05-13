

package com.vero.coreprocessor.components;

import lombok.extern.slf4j.*;
import org.jpos.core.*;
import org.jpos.iso.*;
import org.jpos.util.*;

import java.util.*;

@Slf4j
public class ProtectedLogListener implements LogListener, Configurable {
    
    String[] protectFields = {"2"};
    String[] wipeFields = {"52", "35", "55"};
    Configuration cfg = null;
    public static final String WIPED = "[WIPED]";
    public static final byte[] BINARY_WIPED = ISOUtil.hex2byte("AA55AA55");

    public ProtectedLogListener() {
    }

    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        this.protectFields = ISOUtil.toStringArray("2");
        this.wipeFields = ISOUtil.toStringArray("52,35");
    }

    public synchronized LogEvent log(LogEvent ev) {
        synchronized (ev.getPayLoad()) {
            final List<Object> payLoad = ev.getPayLoad();
            int size = payLoad.size();
            ISOMsg m = null;
            for (int i = 0; i < size; i++) {
                Object obj = payLoad.get(i);
                if (obj instanceof ISOMsg) {
                    m = (ISOMsg) ((ISOMsg) obj).clone();
                    if (m.hasField(2))
                        m.set(2, ISOUtil.protect(m.getString(2)));
                    if (m.hasField(35))
                        m.set(35, ISOUtil.protect(m.getString(35)));
                    if (m.hasField(52))
                        m.set(52, BINARY_WIPED);
                    if (m.hasField(55))
                        m.set(55, "{WIPED}");
                    payLoad.set(i, m);
                }
            }
            ev.dump(System.out, "");

        }
        return ev;
    }

    private void checkProtected(ISOMsg m) throws ISOException {
        String[] var2 = this.protectFields;
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String f = var2[var4];
            Object v = null;

            try {
                v = m.getValue(f);
            } catch (ISOException var8) {
            }

            if (v != null) {
                if (v instanceof String) {
                    m.set(f, ISOUtil.protect((String) v));
                } else {
                    m.set(f, BINARY_WIPED);
                }
            }
        }

    }

    public void protectField(ISOMsg m) throws ISOException {
        String[] var2 = this.protectFields;
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String f = var2[var4];
            Object v = null;

            try {
                v = m.getValue(f);
            } catch (ISOException var8) {
            }

            if (v != null) {
                if (v instanceof String) {
                    m.set(f, ISOUtil.protect((String) v));
                } else {
                    m.set(f, BINARY_WIPED);
                }
            }
        }

    }

    public void wipeField(ISOMsg m) throws ISOException {
        String[] var2 = this.wipeFields;
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String f = var2[var4];
            Object v = null;

            try {
                v = m.getValue(f);
            } catch (ISOException var8) {
            }

            if (v != null) {
                if (v instanceof String) {
                    m.set(f, "[WIPED]");
                } else {
                    m.set(f, BINARY_WIPED);
                }
            }
        }

    }

    private void checkProtected(SimpleMsg sm) throws ISOException {
        if (sm.getMsgContent() instanceof SimpleMsg[]) {
            SimpleMsg[] var2 = (SimpleMsg[]) ((SimpleMsg[]) sm.getMsgContent());
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                SimpleMsg sMsg = var2[var4];
                this.checkProtected(sMsg);
            }
        } else if (sm.getMsgContent() instanceof SimpleMsg) {
            this.checkProtected((SimpleMsg) sm.getMsgContent());
        } else if (sm.getMsgContent() instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) ((ISOMsg) sm.getMsgContent()).clone();
            this.checkProtected(m);
            this.checkHidden(m);
            sm.setMsgContent(m);
        }

    }

    private void checkHidden(ISOMsg m) throws ISOException {
        String[] var2 = this.wipeFields;
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String f = var2[var4];
            Object v = null;

            try {
                v = m.getValue(f);
            } catch (ISOException var8) {
                log.error(var8.toString());
            }

            if (v != null) {
                if (v instanceof String) {
                    m.set(f, "[WIPED]");
                } else {
                    m.set(f, BINARY_WIPED);
                }
            }
        }

    }
}
