package com.cartwave.jobs;

import com.cartwave.email.service.EmailQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailQueueProcessor {

    private final EmailQueueService emailQueueService;

    @Scheduled(fixedDelayString = "${cartwave.jobs.email-dispatch-ms:30000}")
    public void run() {
        emailQueueService.dispatchPendingEmails();
        log.debug("EmailQueueProcessor cycle complete");
    }
}
