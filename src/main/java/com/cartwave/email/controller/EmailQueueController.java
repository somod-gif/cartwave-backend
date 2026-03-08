package com.cartwave.email.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.email.dto.EmailEnqueueRequest;
import com.cartwave.email.dto.EmailQueueResponse;
import com.cartwave.email.service.EmailQueueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/emails")
public class EmailQueueController {

    private final EmailQueueService emailQueueService;

    public EmailQueueController(EmailQueueService emailQueueService) {
        this.emailQueueService = emailQueueService;
    }

    @PostMapping("/enqueue")
    public ResponseEntity<ApiResponse<EmailQueueResponse>> enqueue(@Valid @RequestBody EmailEnqueueRequest request) {
        return new ResponseEntity<>(
                ApiResponse.success("Email queued successfully", emailQueueService.enqueue(request)),
                HttpStatus.ACCEPTED
        );
    }
}
