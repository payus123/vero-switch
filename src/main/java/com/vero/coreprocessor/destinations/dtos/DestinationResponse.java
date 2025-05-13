package com.vero.coreprocessor.destinations.dtos;

import com.vero.coreprocessor.destinations.model.*;
import lombok.*;

import java.time.*;
import java.util.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DestinationResponse {
    private String destinationName;
    private String ip;
    private String port;
    private Boolean status;
    private Date lastStatusCheck;
    private String zmk;
    private String zmkKcv;
    private String zpk;
    private String zpkkcv;
    private ZonedDateTime createdAt;
    private ZonedDateTime modifiedAt;

    public DestinationResponse(Destination destination){
        this.destinationName = destination.getDestinationName();
        this.ip = destination.getIp();
        this.port = destination.getPort();
        this.status = destination.getStatus();
        this.lastStatusCheck = destination.getLastStatusCheck();
        this.zmk = destination.getZmk();
        this.zmkKcv = destination.getZmkKcv();
        this.zpk = destination.getZpk();
        this.zpkkcv = destination.getZpkkcv();
        this.createdAt = destination.getCreatedAt();
        this.modifiedAt = destination.getModifiedAt();
    }
}
