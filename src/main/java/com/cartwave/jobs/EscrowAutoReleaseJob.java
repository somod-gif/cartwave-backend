package com.cartwave.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EscrowAutoReleaseJob {
    private static final Logger log = LoggerFactory.getLogger(EscrowAutoReleaseJob.class);

    @Scheduled(cron = "${cartwave.jobs.escrow-release-cron:0 */15 * * * *}")
    public void run() {
        log.info("Escrow auto-release cycle executed");
    }
}
