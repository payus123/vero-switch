package com.vero.coreprocessor.terminals.model;

import com.vero.coreprocessor.merchants.model.*;
import com.vero.coreprocessor.utils.*;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Table(name = "terminals")
@NoArgsConstructor
@Data
@AllArgsConstructor
public class Terminal extends BaseModel{
    private String terminalId;
    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;
    private Boolean isActive;

}
