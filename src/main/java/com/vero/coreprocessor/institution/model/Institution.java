package com.vero.coreprocessor.institution.model;

import com.vero.coreprocessor.utils.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "institutions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Institution extends BaseModel {
    private String institutionId;
    private String institutionName;
    private String resourceUrl;
    private Boolean isActive;
    private String countryCode;
    private String currencyCode;
    @Convert(converter = StringListConverter.class)
    private List<String> nibssTids;
}
