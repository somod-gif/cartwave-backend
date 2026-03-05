package com.cartwave.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailEnqueueRequest {
    @Email
    @NotBlank
    private String recipient;
    @NotBlank
    private String subject;
    @NotBlank
    private String templateName;
    private String payloadJson;
}
