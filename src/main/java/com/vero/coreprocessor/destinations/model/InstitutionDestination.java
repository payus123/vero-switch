package com.vero.coreprocessor.destinations.model;

import com.vero.coreprocessor.utils.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "institution_destination")
public class InstitutionDestination extends BaseModel {
    private String institutionId;
    @ManyToOne
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;
}
