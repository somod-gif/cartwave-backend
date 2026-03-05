package com.cartwave.email.dto;

import com.cartwave.email.entity.EmailStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EmailQueueResponse {
    private UUID id;
    private String recipient;
    private String subject;
    private String templateName;
    private EmailStatus status;
    private Integer retryCount;
}
