package com.cartwave.subscription.service;

import com.cartwave.exception.LimitExceededException;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.store.entity.Store;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.subscription.dto.SubscriptionChangeRequest;
import com.cartwave.subscription.dto.SubscriptionDTO;
import com.cartwave.subscription.dto.SubscriptionPlanDTO;
import com.cartwave.subscription.entity.Subscription;
import com.cartwave.subscription.entity.SubscriptionPlan;
import com.cartwave.subscription.entity.SubscriptionStatus;
import com.cartwave.subscription.repository.SubscriptionPlanRepository;
import com.cartwave.subscription.repository.SubscriptionRepository;
import com.cartwave.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final StoreRepository storeRepository;

    public SubscriptionDTO cancelSubscription() {
        UUID storeId = TenantContext.getTenantId();
        Subscription subscription = subscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "storeId", storeId));
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setAutoRenewal(false);
        return toDto(subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public SubscriptionDTO getSubscriptionForStore() {
        UUID storeId = TenantContext.getTenantId();
        Subscription subscription = subscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "storeId", storeId));
        return toDto(subscription);
    }

    @Cacheable("subscription-plans")
    @Transactional(readOnly = true)
    public List<SubscriptionPlanDTO> listPlans() {
        return subscriptionPlanRepository.findByActiveTrueAndDeletedFalse().stream().map(this::toPlanDto).toList();
    }

    @CacheEvict(value = "subscription-plans", allEntries = true)
    public SubscriptionDTO changePlan(SubscriptionChangeRequest request) {
        UUID storeId = TenantContext.getTenantId();
        SubscriptionPlan plan = subscriptionPlanRepository.findByName(request.getPlanName())
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "name", request.getPlanName()));

        Subscription subscription = subscriptionRepository.findByStoreId(storeId)
                .orElseGet(Subscription::new);
        long now = Instant.now().toEpochMilli();
        long renewal = Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli();

        subscription.setStoreId(storeId);
        subscription.setPlanId(plan.getId());
        subscription.setPlanName(plan.getName());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(now);
        subscription.setEndDate(renewal);
        subscription.setRenewalDate(renewal);
        subscription.setAmount(plan.getPrice());
        subscription.setBillingCycle(request.getBillingCycle() == null ? "MONTHLY" : request.getBillingCycle());
        subscription.setAutoRenewal(request.getAutoRenewal() == null || request.getAutoRenewal());
        subscription.setFeatures(buildFeatures(plan));

        Subscription saved = subscriptionRepository.save(subscription);
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));
        store.setSubscriptionPlan(plan.getName());
        storeRepository.save(store);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public SubscriptionPlan getPlanForStore(UUID storeId) {
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByStoreId(storeId);
        if (subscriptionOpt.isPresent()) {
            Subscription subscription = subscriptionOpt.get();
            if (subscription.getPlanId() != null) {
                return subscriptionPlanRepository.findById(subscription.getPlanId())
                        .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", subscription.getPlanId()));
            }
            return subscriptionPlanRepository.findByName(subscription.getPlanName())
                    .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "name", subscription.getPlanName()));
        }
        return subscriptionPlanRepository.findByName("FREE")
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "name", "FREE"));
    }

    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(UUID storeId, String featureKey) {
        SubscriptionPlan plan = getPlanForStore(storeId);
        return switch (featureKey) {
            case "payments" -> Boolean.TRUE.equals(plan.getPaymentsEnabled());
            case "custom_domain" -> Boolean.TRUE.equals(plan.getCustomDomainEnabled());
            default -> false;
        };
    }

    @Transactional(readOnly = true)
    public Integer getProductLimit(UUID storeId) {
        return getPlanForStore(storeId).getProductLimit();
    }

    @Transactional(readOnly = true)
    public Integer getStaffLimit(UUID storeId) {
        return getPlanForStore(storeId).getStaffLimit();
    }

    public void assertCanCreateProducts(UUID storeId, long currentCount, long creating) {
        Integer limit = getProductLimit(storeId);
        if (limit != null && limit > 0 && currentCount + creating > limit) {
            throw new LimitExceededException("Product limit exceeded for the current plan.");
        }
    }

    public void assertCanAddStaff(UUID storeId, long currentCount, long adding) {
        Integer limit = getStaffLimit(storeId);
        if (limit != null && limit > 0 && currentCount + adding > limit) {
            throw new LimitExceededException("Staff limit exceeded for the current plan.");
        }
    }

    @Transactional(readOnly = true)
    public List<Subscription> getExpirableSubscriptions(long now) {
        return subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE).stream()
                .filter(subscription -> subscription.getEndDate() != null && subscription.getEndDate() <= now)
                .toList();
    }

    private SubscriptionDTO toDto(Subscription subscription) {
        return SubscriptionDTO.builder()
                .id(subscription.getId())
                .storeId(subscription.getStoreId())
                .planName(subscription.getPlanName())
                .status(subscription.getStatus().name())
                .amount(subscription.getAmount())
                .billingCycle(subscription.getBillingCycle())
                .autoRenewal(subscription.getAutoRenewal())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .renewalDate(subscription.getRenewalDate())
                .features(subscription.getFeatures())
                .planId(subscription.getPlanId())
                .createdAt(subscription.getCreatedAt())
                .build();
    }

    private SubscriptionPlanDTO toPlanDto(SubscriptionPlan plan) {
        return SubscriptionPlanDTO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .productLimit(plan.getProductLimit())
                .staffLimit(plan.getStaffLimit())
                .paymentsEnabled(plan.getPaymentsEnabled())
                .customDomainEnabled(plan.getCustomDomainEnabled())
                .price(plan.getPrice())
                .active(plan.getActive())
                .build();
    }

    private String buildFeatures(SubscriptionPlan plan) {
        return "payments=" + Boolean.TRUE.equals(plan.getPaymentsEnabled())
                + ",custom_domain=" + Boolean.TRUE.equals(plan.getCustomDomainEnabled());
    }
}
