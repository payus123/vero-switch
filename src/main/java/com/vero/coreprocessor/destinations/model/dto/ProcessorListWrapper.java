package com.vero.coreprocessor.destinations.model.dto;

import lombok.*;

import java.util.*;

@Getter
@AllArgsConstructor
public class ProcessorListWrapper {
    private final List<String> processors;
    private final boolean isDefault;
}
