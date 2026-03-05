package com.cartwave.jobs;

import com.cartwave.email.service.EmailQueueService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailDispatcherJob {
    private final EmailQueueService emailQueueService;

    public EmailDispatcherJob(EmailQueueService emailQueueService) {
        this.emailQueueService = emailQueueService;
    }

    @Scheduled(fixedDelayString = "${cartwave.jobs.email-dispatch-ms:30000}")
    public void run() {
        emailQueueService.dispatchPendingEmails();
    }
}
