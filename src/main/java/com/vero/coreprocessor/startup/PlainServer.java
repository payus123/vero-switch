package com.vero.coreprocessor.startup;

import com.vero.coreprocessor.components.ProtectedLogListener;
import com.vero.coreprocessor.listeners.IncomingListener;
import jakarta.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.core.*;
import org.jpos.iso.*;
import org.jpos.iso.channel.*;
import org.jpos.iso.packager.*;
import org.jpos.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlainServer implements CommandLineRunner {
    @Value("${spring.profiles.active}")
    String environment;
    @Value("${plain.server.port}")
    int port;
    private final ApplicationContext applicationContext;
    private final IncomingListener incomingListener;
    Logger logger = new Logger();
    ISOServer server;

    @Override
    @SuppressWarnings("deprecation")
    public void run(String... args) throws Exception {
        if (environment.equals("prod") || environment.equals("staging")) {
            logger.addListener(new ProtectedLogListener());
        }
        else
            logger.addListener(new SimpleLogListener(System.out));

        ServerChannel serverChannel = new PostChannel(new GenericPackager("nibss.xml"));
        ((LogSource)serverChannel).setLogger(logger,"incoming-request");

        ThreadPool pool = new ThreadPool(200,500);
        server = new ISOServer(port,serverChannel,pool);

        ((LogSource)server).setLogger(logger,"server");

        applicationContext.getAutowireCapableBeanFactory().autowireBean(incomingListener);
        final Properties properties = new Properties();
        properties.put("keep-channels", false);
        final SimpleConfiguration configuration = new SimpleConfiguration(properties);
        server.setConfiguration(configuration);
        server.addISORequestListener(incomingListener);
        log.info("============= starting server on port {} =============",port);
        new Thread(server).start();

    }

    @PreDestroy
    void shutDown(){
        log.info("==============shutting down server==============");
        server.shutdown();


    }
}
