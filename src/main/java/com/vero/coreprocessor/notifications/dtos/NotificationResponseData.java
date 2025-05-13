package com.vero.coreprocessor.notifications.dtos;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationResponseData {
    private String jobId;
    private String status;

}
