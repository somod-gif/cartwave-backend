package com.cartwave.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FraudScannerJob {
    private static final Logger log = LoggerFactory.getLogger(FraudScannerJob.class);

    @Scheduled(cron = "${cartwave.jobs.fraud-scan-cron:0 */10 * * * *}")
    public void run() {
        log.info("Fraud scanner cycle executed");
    }
}
