package com.vero.coreprocessor.merchants.model;

import com.vero.coreprocessor.institution.model.*;
import com.vero.coreprocessor.utils.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "merchant")
public class Merchant extends BaseModel {
    private String merchantId;
    @ManyToOne
    @JoinColumn(name = "institution_id")
    private Institution institution;
    private String merchantUniqueReference;
    private String countryCode;
    private String currencyCode;
    private String merchantCategoryCode;
    private String merchantNameAndLocation;
    private boolean isActive;
    private boolean isRoutingEnabled;

}