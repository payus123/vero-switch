package com.vero.coreprocessor.notifications.dtos;

import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRequest {
  private List<String> to;
  private List<String> cc;
  private String stylingContent;
  private Map<String, String> variables;
  private String templateType;
  private String fileName;
  private String encodedFile;
  private String subject;
  private String content;
}