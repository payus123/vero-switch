package com.vero.coreprocessor.destinations.service;

import com.vero.coreprocessor.destinations.model.*;
import com.vero.coreprocessor.destinations.repository.*;
import com.vero.coreprocessor.exceptions.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.jpos.util.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckDestinationUptimeService {
    private final DestinationRepository destinationRepository;
//    @Scheduled(fixedDelay = 20000)
    public void doCheck(){
        log.info("starting destination status checks");
        check();
    }



    void check(){
        List<Destination> destinations = destinationRepository.findAll();
        for (Destination destination:destinations){
            log.info("checking status for {}",destination.getDestinationName());
            destination.setLastStatusCheck(new Date());
            try {
                MUX mux = NameRegistrar.getIfExists("mux." + destination.getDestinationName());
                if (mux==null||!mux.isConnected()){
                    destination.setStatus(Boolean.FALSE);
                }
                else {
                    ISOMsg req = createMsg();
                    ISOMsg response = mux.request(req, 15000);
                    if (response==null){
                        destination.setStatus(Boolean.FALSE);
                    }
                    else if (response.hasField(39)&& response.getString(39).equals("00")){
                        destination.setStatus(Boolean.TRUE);
                    }
                    else {
                        destination.setStatus(Boolean.FALSE);
                    }
                }
                destinationRepository.save(destination);
            } catch (ISOException e) {
                throw new StatusCheckException(e.getMessage());
            }
        }

    }

    static ISOMsg createMsg(){
        ISOMsg msg = new ISOMsg();
        msg.set(0, "0800");
        msg.set(3,"NC0000");
        msg.set(7, ISODate.getDateTime(new Date()));
        msg.set(11,"000001");
        msg.set(12,ISODate.getTime(new Date()));
        msg.set(13,ISODate.getDate(new Date()));
        msg.set(70,"001");

        return msg;
    }
}
