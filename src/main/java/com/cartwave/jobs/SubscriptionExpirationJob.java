package com.cartwave.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionExpirationJob {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionExpirationJob.class);

    @Scheduled(cron = "${cartwave.jobs.subscription-expiration-cron:0 0 * * * *}")
    public void run() {
        log.info("Subscription expiration cycle executed");
    }
}
