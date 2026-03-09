package com.cartwave.email.entity;

import com.cartwave.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "email_queue")
public class EmailQueue extends BaseEntity {

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status = EmailStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "error_message")
    private String errorMessage;

    /** Timestamp when the email was successfully dispatched */
    @Column(name = "sent_at")
    private Instant sentAt;
}
