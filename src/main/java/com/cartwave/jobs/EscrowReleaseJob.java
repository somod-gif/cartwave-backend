package com.cartwave.jobs;

import com.cartwave.escrow.service.EscrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EscrowReleaseJob {

    private final EscrowService escrowService;

    /**
     * Daily sweep: release any held escrows whose release time has passed.
     */
    @Scheduled(cron = "${cartwave.jobs.escrow-release-cron:0 0 2 * * *}")
    public void run() {
        long now = System.currentTimeMillis();
        escrowService.processReleasable(now);
        log.debug("EscrowReleaseJob executed at {}", now);
    }
}
