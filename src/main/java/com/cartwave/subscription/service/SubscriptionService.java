package com.cartwave.subscription.service;

import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.subscription.dto.SubscriptionDTO;
import com.cartwave.subscription.entity.Subscription;
import com.cartwave.subscription.mapper.SubscriptionMapper;
import com.cartwave.subscription.repository.SubscriptionRepository;
import com.cartwave.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional(readOnly = true)
    public SubscriptionDTO getSubscriptionForStore() {
        log.info("Fetching subscription for store");
        var storeId = TenantContext.getTenantId();
        
        Subscription subscription = subscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "storeId", storeId));

        return subscriptionMapper.toSubscriptionDTO(subscription);
    }

}
