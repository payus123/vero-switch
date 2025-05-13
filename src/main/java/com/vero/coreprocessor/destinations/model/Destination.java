package com.vero.coreprocessor.destinations.model;

import com.vero.coreprocessor.destinations.enums.*;
import com.vero.coreprocessor.utils.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "destination")
public class Destination extends BaseModel {
    @Column(unique = true)
    private String destinationName;
    private String ip;
    private String port;
    private Boolean status;
    private Date lastStatusCheck;
    @Enumerated(EnumType.STRING)
    private DestinationDomain domain;
    @Column(length = 32)
    private String zmk;
    @Column(length = 6)
    private String zmkKcv;
    @Column(length = 32)
    private String zpk;
    @Column(length = 6)
    private String zpkkcv;
    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InstitutionDestination> institutionDestinations;


}
