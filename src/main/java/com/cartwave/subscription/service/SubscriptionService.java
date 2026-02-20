package com.cartwave.subscription.service;

import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.exception.LimitExceededException;
import com.cartwave.subscription.dto.SubscriptionDTO;
import com.cartwave.subscription.entity.Subscription;
import com.cartwave.subscription.entity.SubscriptionPlan;
import com.cartwave.subscription.mapper.SubscriptionMapper;
import com.cartwave.subscription.repository.SubscriptionPlanRepository;
import com.cartwave.subscription.repository.SubscriptionRepository;
import com.cartwave.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Transactional(readOnly = true)
    public SubscriptionDTO getSubscriptionForStore() {
        log.info("Fetching subscription for store");
        var storeId = TenantContext.getTenantId();

        Subscription subscription = subscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "storeId", storeId));

        return subscriptionMapper.toSubscriptionDTO(subscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionPlan getPlanForStore(UUID storeId) {
        // Find active subscription for store and resolve plan
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByStoreId(storeId);
        if (subscriptionOpt.isPresent()) {
            String planName = subscriptionOpt.get().getPlanName();
            return subscriptionPlanRepository.findByName(planName)
                    .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "name", planName));
        }
        // Fallback to FREE plan
        return subscriptionPlanRepository.findByName("FREE")
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "name", "FREE"));
    }

    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(UUID storeId, String featureKey) {
        SubscriptionPlan plan = getPlanForStore(storeId);
        switch (featureKey) {
            case "payments":
                return Boolean.TRUE.equals(plan.getPaymentsEnabled());
            case "custom_domain":
                return Boolean.TRUE.equals(plan.getCustomDomainEnabled());
            default:
                return false;
        }
    }

    @Transactional(readOnly = true)
    public Integer getProductLimit(UUID storeId) {
        SubscriptionPlan plan = getPlanForStore(storeId);
        return plan.getProductLimit();
    }

    @Transactional(readOnly = true)
    public Integer getStaffLimit(UUID storeId) {
        SubscriptionPlan plan = getPlanForStore(storeId);
        return plan.getStaffLimit();
    }

    /**
     * Helper that throws LimitExceededException when trying to exceed product limit.
     */
    public void assertCanCreateProducts(UUID storeId, long currentCount, long creating) {
        Integer limit = getProductLimit(storeId);
        if (limit != null && limit > 0) {
            if (currentCount + creating > limit) {
                throw new LimitExceededException("Product limit exceeded for plan. Allowed=" + limit + " current=" + currentCount);
            }
        }
    }

    public void assertCanAddStaff(UUID storeId, long currentCount, long adding) {
        Integer limit = getStaffLimit(storeId);
        if (limit != null && limit > 0) {
            if (currentCount + adding > limit) {
                throw new LimitExceededException("Staff limit exceeded for plan. Allowed=" + limit + " current=" + currentCount);
            }
        }
    }

}
