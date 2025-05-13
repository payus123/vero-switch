// 
// Decompiled by Procyon v0.5.36
// 

package com.vero.coreprocessor.logonmanagers;

import org.jdom2.*;
import org.jdom2.output.*;
import org.jpos.core.*;
import org.jpos.iso.*;
import org.jpos.iso.packager.*;
import org.jpos.q2.*;
import org.jpos.space.*;
import org.jpos.util.*;

import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class LogonManager extends QBeanSupport implements Runnable {
    Space sp;
    Space psp;
    MUX mux;
    long timeout;
    long echoInterval;
    long logonInterval;
    long initialDelay;
    String readyKey;
    ISOMsg logonMsg;
    ISOMsg logoffMsg;
    ISOMsg echoMsg;
    Date now = new Date();
    public static final String TRACE = "ISW_TRACE";
    public static final String LOGON = "ISW_LOGON.";
    public static final String ECHO  = "ISW_ECHO.";
    public static final String FDR_KEY = "ISW_KEY";
    private boolean isSignOn = false;

    public void initService () throws ConfigurationException {
        Configuration cfg = getConfiguration();
        sp       = SpaceFactory.getSpace (cfg.get ("space", ""));
        psp      = SpaceFactory.getSpace (cfg.get ("persistent-space", ""));
        timeout  = cfg.getLong ("timeout", 30000);
        echoInterval  = cfg.getLong ("echo-interval", 30000);
        logonInterval = cfg.getLong ("logon-interval", 86400000L);
        initialDelay  = cfg.getLong ("initial-delay", 1000L);
        readyKey      = cfg.get ("channel-ready");
        Element config = getPersist();
        logonMsg      =  getMsg ("logon", config);
        logoffMsg     =  getMsg ("logoff", config);
        echoMsg       =  getMsg ("echo", config);
    }
    public void startService () {
        try {
            mux  = NameRegistrar.get ("mux." + cfg.get ("mux"));
        } catch (NameRegistrar.NotFoundException e) {
            getLog().warn (e);
        }
        new Thread (this).start();
    }
    public void run () {
        while (running()) {
            Object sessionId = sp.rd (readyKey, 60000);
            if (sessionId == null) {
                getLog().info ("Channel " + readyKey + " not ready");
                continue;
            }
            try {
                if (!sessionId.equals (sp.rdp (LOGON+readyKey))) {
                    doEcho();

                    Thread.sleep (10000);
                } else if (sp.rdp (ECHO+readyKey) == null) {
                    doEcho ();
                }
            } catch (Throwable t) {
                getLog().warn (t);
            }
            ISOUtil.sleep (20000);
        }
    }
    public void stopService() {
        try {
            doLogoff();
        } catch (Throwable t) {
            getLog().warn (t);
        }
    }

    public void doSignOn() throws ISOException{
        SpaceUtil.wipe (sp, LOGON+readyKey);
        mux.request (createMsg ("101", logoffMsg), 1000);
    }

    private void doLogon (Object sessionId) throws ISOException {
        ISOMsg resp = mux.request (createMsg ("001", logonMsg), timeout);
        if (resp != null && "0000".equals (resp.getString(39))) {   // RC will come in CMF, hence 0000
            SpaceUtil.wipe (sp, LOGON+readyKey);
            sp.out (LOGON+readyKey, sessionId, logonInterval);
            getLog().info (
                    "Logon successful (session ID " + sessionId.toString() + ")"
            );
        }
    }

    private void doLogoff () throws ISOException {
        SpaceUtil.wipe (sp, LOGON+readyKey);
        mux.request (createMsg ("002", logoffMsg), 1000);
    }

    private void doEcho () throws ISOException {
        ISOMsg msg = createMsg("101", echoMsg);
        msg.set(3,"NC0000");
        ISOMsg resp = mux.request (msg, timeout);
        if (resp != null) {
            sp.out (ECHO+readyKey, new Object(), echoInterval);
        }
    }

    private ISOMsg createMsg (String msgType, ISOMsg merge) throws ISOException
    {
        long traceNumber = SpaceUtil.nextLong (psp, TRACE) % 1000000;
        ISOMsg m = new ISOMsg("0800");                                // use CMF specs for MTI
        m.set(7, ISODate.getDateTime(new Date()));
        m.set(11, ISOUtil.zeropad (Long.toString(traceNumber), 6));   // we can leave STAN with 6 figures
        m.set(12, ISODate.getTime(now));
        m.set(13, ISODate.getDate(now));
        m.set(70, msgType);
        if (merge != null)
            m.merge (merge);
        return m;
    }

    // Gets isomsg chunk for each type of network message: logonMsg, logoffMsg. echoMsg
    private ISOMsg getMsg (String name, Element config) throws ConfigurationException
    {
        ISOMsg m = new ISOMsg();
        Element e = config.getChild (name);
        if (e != null)
            e = e.getChild ("isomsg");
        if (e != null) {
            try {
                XMLPackager p = new XMLPackager();
                p.setLogger (getLog().getLogger(), getLog().getRealm()
                        + "-config-" + name);
                m.setPackager (p);

                ByteArrayOutputStream os = new ByteArrayOutputStream ();
                OutputStreamWriter writer = new OutputStreamWriter (os);
                XMLOutputter out = new XMLOutputter ();
                out.output (e, writer);
                writer.close ();
                m.unpack (os.toByteArray());
            } catch (Exception ex) {
                throw new ConfigurationException (ex);
            }
        }
        return m;
    }
}