package com.cartwave.email.service;

import com.cartwave.email.dto.EmailEnqueueRequest;
import com.cartwave.email.dto.EmailQueueResponse;
import com.cartwave.email.entity.EmailQueue;
import com.cartwave.email.entity.EmailStatus;
import com.cartwave.email.repository.EmailQueueRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class EmailQueueService {

    private static final int MAX_RETRY = 3;
    private final EmailQueueRepository emailQueueRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    @Value("${spring.mail.from:noreply@cartwave.store}")
    private String mailFrom;

    public EmailQueueService(EmailQueueRepository emailQueueRepository,
                             JavaMailSender mailSender,
                             TemplateEngine templateEngine,
                             ObjectMapper objectMapper) {
        this.emailQueueRepository = emailQueueRepository;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public EmailQueueResponse enqueue(EmailEnqueueRequest request) {
        EmailQueue queue = new EmailQueue();
        queue.setRecipient(request.getRecipient());
        queue.setSubject(request.getSubject());
        queue.setTemplateName(request.getTemplateName());
        queue.setPayloadJson(request.getPayloadJson());
        queue.setStatus(EmailStatus.PENDING);
        EmailQueue saved = emailQueueRepository.save(queue);
        return EmailQueueResponse.builder()
                .id(saved.getId())
                .recipient(saved.getRecipient())
                .subject(saved.getSubject())
                .templateName(saved.getTemplateName())
                .status(saved.getStatus())
                .retryCount(saved.getRetryCount())
                .build();
    }

    @Transactional
    public void dispatchPendingEmails() {
        List<EmailQueue> pending = emailQueueRepository.findByStatusOrderByCreatedAtAsc(
                EmailStatus.PENDING, PageRequest.of(0, 50));
        for (EmailQueue email : pending) {
            try {
                String html = renderTemplate(email.getTemplateName(), email.getPayloadJson());
                var mimeMessage = mailSender.createMimeMessage();
                var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(mailFrom);
                helper.setTo(email.getRecipient());
                helper.setSubject(email.getSubject());
                helper.setText(html, true);
                mailSender.send(mimeMessage);
                email.setStatus(EmailStatus.SENT);
                email.setSentAt(java.time.Instant.now());
                email.setErrorMessage(null);
            } catch (Exception ex) {
                int retries = email.getRetryCount() + 1;
                email.setRetryCount(retries);
                email.setErrorMessage(ex.getMessage());
                if (retries >= MAX_RETRY) {
                    email.setStatus(EmailStatus.FAILED);
                }
                log.warn("Email dispatch failed for id={}: {}", email.getId(), ex.getMessage());
            }
            emailQueueRepository.save(email);
        }
    }

    // ── Template rendering ────────────────────────────────────────────────────

    private String renderTemplate(String templateName, String payloadJson) {
        Map<String, Object> variables = parsePayload(payloadJson);
        Context ctx = new Context();
        variables.forEach(ctx::setVariable);
        String templatePath = "email/" + templateName;
        try {
            return templateEngine.process(templatePath, ctx);
        } catch (Exception e) {
            // Fallback to plain text if template not found
            log.warn("Template '{}' not found, falling back to plain text", templatePath);
            return "<pre>" + payloadJson + "</pre>";
        }
    }

    private Map<String, Object> parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse email payload JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    // ── Typed convenience methods ─────────────────────────────────────────────

    public void enqueueEscrowReleased(UUID storeId, UUID escrowId, BigDecimal sellerAmount) {
        try {
            EmailEnqueueRequest req = new EmailEnqueueRequest();
            req.setSubject("Escrow Payment Released");
            req.setTemplateName("escrow_released");
            req.setPayloadJson(String.format("{\"storeId\":\"%s\",\"escrowId\":\"%s\",\"sellerAmount\":\"%s\"}", storeId, escrowId, sellerAmount));
            req.setRecipient("noreply@cartwave.store");
            enqueue(req);
        } catch (Exception e) {
            log.warn("enqueueEscrowReleased failed: {}", e.getMessage());
        }
    }

    public void enqueueDisputeOpened(UUID storeId, UUID escrowId, UUID raisedByUserId) {
        try {
            EmailEnqueueRequest req = new EmailEnqueueRequest();
            req.setSubject("Dispute Raised on Your Escrow");
            req.setTemplateName("dispute_opened");
            req.setPayloadJson(String.format("{\"storeId\":\"%s\",\"escrowId\":\"%s\",\"raisedByUserId\":\"%s\"}", storeId, escrowId, raisedByUserId));
            req.setRecipient("noreply@cartwave.store");
            enqueue(req);
        } catch (Exception e) {
            log.warn("enqueueDisputeOpened failed: {}", e.getMessage());
        }
    }

    public void enqueueDisputeResolved(UUID storeId, UUID disputeId, UUID userId) {
        try {
            EmailEnqueueRequest req = new EmailEnqueueRequest();
            req.setSubject("Your Dispute Has Been Resolved");
            req.setTemplateName("dispute_resolved");
            req.setPayloadJson(String.format("{\"storeId\":\"%s\",\"disputeId\":\"%s\",\"userId\":\"%s\"}", storeId, disputeId, userId));
            req.setRecipient("noreply@cartwave.store");
            enqueue(req);
        } catch (Exception e) {
            log.warn("enqueueDisputeResolved failed: {}", e.getMessage());
        }
    }

    public void enqueueOrderConfirmed(String recipient, UUID orderId) {
        try {
            EmailEnqueueRequest req = new EmailEnqueueRequest();
            req.setRecipient(recipient);
            req.setSubject("Order Confirmed ✓");
            req.setTemplateName("order_confirmed");
            req.setPayloadJson(String.format("{\"orderId\":\"%s\"}", orderId));
            enqueue(req);
        } catch (Exception e) {
            log.warn("enqueueOrderConfirmed failed: {}", e.getMessage());
        }
    }

    public void enqueueOrderShipped(String recipient, UUID orderId) {
        try {
            EmailEnqueueRequest req = new EmailEnqueueRequest();
            req.setRecipient(recipient);
            req.setSubject("Your Order Has Shipped 🚚");
            req.setTemplateName("order_shipped");
            req.setPayloadJson(String.format("{\"orderId\":\"%s\"}", orderId));
            enqueue(req);
        } catch (Exception e) {
            log.warn("enqueueOrderShipped failed: {}", e.getMessage());
        }
    }

    public void enqueueOrderDelivered(String recipient, UUID orderId) {
        try {
            EmailEnqueueRequest req = new EmailEnqueueRequest();
            req.setRecipient(recipient);
            req.setSubject("Order Delivered ✅");
            req.setTemplateName("order_delivered");
            req.setPayloadJson(String.format("{\"orderId\":\"%s\"}", orderId));
            enqueue(req);
        } catch (Exception e) {
            log.warn("enqueueOrderDelivered failed: {}", e.getMessage());
        }
    }

    public void enqueuePasswordReset(String recipient, String resetLink) {
        try {
            EmailEnqueueRequest req = new EmailEnqueueRequest();
            req.setRecipient(recipient);
            req.setSubject("Reset Your Password");
            req.setTemplateName("password_reset");
            req.setPayloadJson(String.format("{\"resetLink\":\"%s\"}", resetLink));
            enqueue(req);
        } catch (Exception e) {
            log.warn("enqueuePasswordReset failed: {}", e.getMessage());
        }
    }

    public void enqueueEmailVerification(String recipient, String firstName, String verificationLink) {
        try {
            EmailEnqueueRequest req = new EmailEnqueueRequest();
            req.setRecipient(recipient);
            req.setSubject("Verify Your CartWave Email Address");
            req.setTemplateName("email_verification");
            req.setPayloadJson(String.format("{\"firstName\":\"%s\",\"verificationLink\":\"%s\"}", firstName, verificationLink));
            enqueue(req);
        } catch (Exception e) {
            log.warn("enqueueEmailVerification failed: {}", e.getMessage());
        }
    }

    public void enqueueSubscriptionExpiring(String recipient, String planName, String expiresAt) {
        try {
            EmailEnqueueRequest req = new EmailEnqueueRequest();
            req.setRecipient(recipient);
            req.setSubject("Your Subscription Is Expiring Soon ⚠️");
            req.setTemplateName("subscription_expiring");
            req.setPayloadJson(String.format("{\"planName\":\"%s\",\"expiresAt\":\"%s\"}", planName, expiresAt));
            enqueue(req);
        } catch (Exception e) {
            log.warn("enqueueSubscriptionExpiring failed: {}", e.getMessage());
        }
    }
}
