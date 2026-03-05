package com.cartwave.email.service;

import com.cartwave.email.dto.EmailEnqueueRequest;
import com.cartwave.email.dto.EmailQueueResponse;
import com.cartwave.email.entity.EmailQueue;
import com.cartwave.email.entity.EmailStatus;
import com.cartwave.email.repository.EmailQueueRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailQueueService {

    private static final int MAX_RETRY = 3;
    private final EmailQueueRepository emailQueueRepository;
    private final JavaMailSender mailSender;

    public EmailQueueService(EmailQueueRepository emailQueueRepository, JavaMailSender mailSender) {
        this.emailQueueRepository = emailQueueRepository;
        this.mailSender = mailSender;
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
        List<EmailQueue> pending = emailQueueRepository.findByStatusOrderByCreatedAtAsc(EmailStatus.PENDING, PageRequest.of(0, 50));
        for (EmailQueue email : pending) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email.getRecipient());
                message.setSubject(email.getSubject());
                message.setText("Template: " + email.getTemplateName() + "\nPayload: " + email.getPayloadJson());
                mailSender.send(message);
                email.setStatus(EmailStatus.SENT);
                email.setErrorMessage(null);
            } catch (Exception ex) {
                int retries = email.getRetryCount() + 1;
                email.setRetryCount(retries);
                email.setErrorMessage(ex.getMessage());
                if (retries >= MAX_RETRY) {
                    email.setStatus(EmailStatus.FAILED);
                }
            }
            emailQueueRepository.save(email);
        }
    }
}
