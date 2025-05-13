package com.vero.coreprocessor.ruleengine.routingtypes.schemeroute;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemeRouteParam{
    private String schemeName;
    private String minimumAmount;
    private String maximumAmount;
}
