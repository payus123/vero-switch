package com.vero.coreprocessor.ruleengine.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.*;
import java.util.*;

@Setter
@Getter
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseRule {
    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long id;

    @JsonProperty("id")
    private String uuid;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    private ZonedDateTime modifiedAt;
    private Long precedence;
    private String ruleName;
    private String condition;
    private String processor;
    private String merchantType;
    private String ownerId;
    private Boolean isActive;
    @PrePersist
    protected void prePersist() {

        uuid = UUID.randomUUID().toString();
    }

}
