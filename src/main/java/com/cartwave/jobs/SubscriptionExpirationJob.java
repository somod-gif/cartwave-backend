package com.cartwave.jobs;

import com.cartwave.subscription.entity.SubscriptionStatus;
import com.cartwave.subscription.repository.SubscriptionRepository;
import com.cartwave.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpirationJob {

    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "${cartwave.jobs.subscription-expiration-cron:0 0 * * * *}")
    public void run() {
        long now = System.currentTimeMillis();
        subscriptionService.getExpirableSubscriptions(now).forEach(subscription -> {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            log.info("Expired subscription {}", subscription.getId());
        });
    }
}
