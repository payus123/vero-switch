package com.vero.coreprocessor.config;

import jakarta.annotation.*;
import org.jpos.q2.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.stereotype.*;

@Component
public class DeployConfigs implements CommandLineRunner
{
    @Value("${deployDir}")
    String deployDir;
    public void run(final String... args) {
        Q2 q2 = new Q2(deployDir);
        q2.start();
    }
    
    @PreDestroy
    void StopService() {
        Q2 runningServer = Q2.getQ2();
        runningServer.shutdown();
    }


}
