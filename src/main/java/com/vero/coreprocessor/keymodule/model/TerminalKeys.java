package com.vero.coreprocessor.keymodule.model;

import com.vero.coreprocessor.utils.*;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "terminal_key")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TerminalKeys extends BaseModel {
    private String tmk;
    private String tmkKcv;
    private String tsk;
    private String tskKcv;
    private String tpk;
    private String tpkKcv;
    private String terminalId;
}
