package com.vero.coreprocessor.notifications.dtos;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationResponse {
    private String message;
    private NotificationResponseData data;
}
