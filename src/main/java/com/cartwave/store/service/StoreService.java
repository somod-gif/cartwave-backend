package com.cartwave.store.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cartwave.exception.BusinessException;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.security.model.CurrentUserPrincipal;
import com.cartwave.security.service.CurrentUserService;
import com.cartwave.store.dto.StoreBrandingRequest;
import com.cartwave.store.dto.StoreCreateRequest;
import com.cartwave.store.dto.StoreDTO;
import com.cartwave.store.dto.StoreDomainRequest;
import com.cartwave.store.dto.StoreSeoRequest;
import com.cartwave.store.dto.StoreUpdateRequest;
import com.cartwave.store.entity.Store;
import com.cartwave.store.entity.StoreStatus;
import com.cartwave.store.entity.StoreTemplate;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.subscription.entity.Subscription;
import com.cartwave.subscription.entity.SubscriptionPlan;
import com.cartwave.subscription.entity.SubscriptionStatus;
import com.cartwave.subscription.repository.SubscriptionPlanRepository;
import com.cartwave.subscription.repository.SubscriptionRepository;
import com.cartwave.subscription.service.SubscriptionService;
import com.cartwave.tenant.TenantContext;
import com.cartwave.user.entity.UserRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionService subscriptionService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<StoreDTO> listStores() {
        CurrentUserPrincipal principal = currentUserService.requireCurrentUser();
        if (principal.getRole() == UserRole.SUPER_ADMIN || principal.getRole() == UserRole.ADMIN) {
            return storeRepository.findAllActive().stream().map(this::toDto).toList();
        }
        if (principal.getRole() == UserRole.BUSINESS_OWNER) {
            return storeRepository.findAllByOwnerId(principal.getUserId()).stream().map(this::toDto).toList();
        }
        return TenantContext.getOptionalTenantId()
                .flatMap(storeRepository::findById)
                .map(this::toDto)
                .stream()
                .toList();
    }

    public StoreDTO createStore(StoreCreateRequest request) {
        CurrentUserPrincipal principal = currentUserService.requireCurrentUser();
        if (principal.getRole() != UserRole.BUSINESS_OWNER
                && principal.getRole() != UserRole.ADMIN
                && principal.getRole() != UserRole.SUPER_ADMIN) {
            throw new BusinessException("STORE_CREATE_FORBIDDEN", "Only owners or admins can create stores.");
        }

        storeRepository.findBySlug(request.getSlug()).ifPresent(existing -> {
            throw new BusinessException("STORE_SLUG_EXISTS", "Store slug already exists.");
        });

        Store store = Store.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .country(request.getCountry())
                .currency(request.getCurrency() == null || request.getCurrency().isBlank() ? "USD" : request.getCurrency())
                .ownerId(principal.getUserId())
                .subscriptionPlan("FREE")
                .isActive(true)
                .logoUrl(request.getLogoUrl())
                .bannerUrl(request.getBannerUrl())
                .websiteUrl(request.getWebsiteUrl())
                .businessAddress(request.getBusinessAddress())
                .businessRegistrationNumber(request.getBusinessRegistrationNumber())
                .businessPhoneNumber(request.getBusinessPhoneNumber())
                .businessEmail(request.getBusinessEmail())
                .subdomain(request.getSlug() + ".cartwave.store")
                .storeStatus(StoreStatus.ACTIVE)
                .build();

        Store savedStore = storeRepository.save(store);
        seedDefaultSubscription(savedStore);
        return toDto(savedStore);
    }

    @Transactional(readOnly = true)
    public StoreDTO getStoreById(UUID storeId) {
        Store store = findAccessibleStore(storeId);
        return toDto(store);
    }

    @CacheEvict(value = "store-public", key = "#storeId")
    public StoreDTO updateStore(UUID storeId, StoreUpdateRequest request) {
        Store store = findAccessibleStore(storeId);

        if (Boolean.TRUE.equals(request.getCustomDomain())) {
            boolean allowed = subscriptionService.isFeatureEnabled(storeId, "custom_domain");
            if (!allowed) {
                throw new BusinessException("CUSTOM_DOMAIN_NOT_ALLOWED", "Current subscription plan does not allow custom domains.");
            }
        }

        if (request.getName() != null) {
            store.setName(request.getName());
        }
        if (request.getDescription() != null) {
            store.setDescription(request.getDescription());
        }
        if (request.getCountry() != null) {
            store.setCountry(request.getCountry());
        }
        if (request.getCurrency() != null) {
            store.setCurrency(request.getCurrency());
        }
        if (request.getSubscriptionPlan() != null) {
            store.setSubscriptionPlan(request.getSubscriptionPlan());
        }
        if (request.getIsActive() != null) {
            store.setIsActive(request.getIsActive());
        }
        if (request.getLogoUrl() != null) {
            store.setLogoUrl(request.getLogoUrl());
        }
        if (request.getBannerUrl() != null) {
            store.setBannerUrl(request.getBannerUrl());
        }
        if (request.getWebsiteUrl() != null) {
            store.setWebsiteUrl(request.getWebsiteUrl());
        }
        if (request.getBusinessAddress() != null) {
            store.setBusinessAddress(request.getBusinessAddress());
        }
        if (request.getBusinessRegistrationNumber() != null) {
            store.setBusinessRegistrationNumber(request.getBusinessRegistrationNumber());
        }
        if (request.getBusinessPhoneNumber() != null) {
            store.setBusinessPhoneNumber(request.getBusinessPhoneNumber());
        }
        if (request.getBusinessEmail() != null) {
            store.setBusinessEmail(request.getBusinessEmail());
        }

        return toDto(storeRepository.save(store));
    }

    public void deleteStore(UUID storeId) {
        Store store = findAccessibleStore(storeId);
        store.setDeleted(true);
        store.setIsActive(false);
        storeRepository.save(store);
    }

    @Cacheable(value = "store-public", key = "'slug:' + #slug")
    @Transactional(readOnly = true)
    public StoreDTO getPublicStoreBySlug(String slug) {
        Store store = storeRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "slug", slug));
        if (!Boolean.TRUE.equals(store.getIsActive())) {
            throw new ResourceNotFoundException("Store", "slug", slug);
        }
        return toDto(store);
    }

    private Store findAccessibleStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));

        CurrentUserPrincipal principal = currentUserService.requireCurrentUser();
        if (principal.getRole() == UserRole.SUPER_ADMIN || principal.getRole() == UserRole.ADMIN) {
            return store;
        }

        if (principal.getRole() == UserRole.BUSINESS_OWNER && store.getOwnerId().equals(principal.getUserId())) {
            return store;
        }

        if (TenantContext.getOptionalTenantId().filter(storeId::equals).isPresent()) {
            return store;
        }

        throw new BusinessException("STORE_ACCESS_DENIED", "You do not have access to this store.");
    }

    private void seedDefaultSubscription(Store store) {
        SubscriptionPlan freePlan = subscriptionPlanRepository.findByName("FREE")
                .orElseGet(() -> subscriptionPlanRepository.save(
                        SubscriptionPlan.builder()
                                .name("FREE")
                                .description("Default free plan")
                                .productLimit(20)
                                .staffLimit(1)
                                .paymentsEnabled(false)
                                .customDomainEnabled(false)
                                .price(BigDecimal.ZERO)
                                .active(true)
                                .build()
                ));

        long now = Instant.now().toEpochMilli();
        long renewal = Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli();
        Subscription subscription = Subscription.builder()
                .storeId(store.getId())
                .planId(freePlan.getId())
                .planName(freePlan.getName())
                .status(SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(renewal)
                .renewalDate(renewal)
                .amount(freePlan.getPrice())
                .billingCycle("MONTHLY")
                .autoRenewal(true)
                .features("payments=false,custom_domain=false")
                .build();
        subscriptionRepository.save(subscription);
    }

    private StoreDTO toDto(Store store) {
        return StoreDTO.builder()
                .id(store.getId())
                .name(store.getName())
                .slug(store.getSlug())
                .description(store.getDescription())
                .country(store.getCountry())
                .currency(store.getCurrency())
                .subscriptionPlan(store.getSubscriptionPlan())
                .isActive(store.getIsActive())
                .logoUrl(store.getLogoUrl())
                .bannerUrl(store.getBannerUrl())
                .websiteUrl(store.getWebsiteUrl())
                .businessAddress(store.getBusinessAddress())
                .businessRegistrationNumber(store.getBusinessRegistrationNumber())
                .businessPhoneNumber(store.getBusinessPhoneNumber())
                .businessEmail(store.getBusinessEmail())
                .customDomain(subscriptionService.isFeatureEnabled(store.getId(), "custom_domain"))
                .ownerId(store.getOwnerId())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                // V2 fields
                .template(store.getTemplate() == null ? null : store.getTemplate().name())
                .brandColor(store.getBrandColor())
                .customDomainName(store.getCustomDomainName())
                .subdomain(store.getSubdomain())
                .storeStatus(store.getStoreStatus() == null ? null : store.getStoreStatus().name())
                .metaTitle(store.getMetaTitle())
                .metaDescription(store.getMetaDescription())
                .keywords(store.getKeywords())
                .build();
    }

    // ── V2 branding / domain / SEO ───────────────────────────────────────────

    @CacheEvict(value = "store-public", key = "#storeId")
    public StoreDTO updateBranding(UUID storeId, StoreBrandingRequest request) {
        Store store = findAccessibleStore(storeId);
        if (request.getLogoUrl() != null)   store.setLogoUrl(request.getLogoUrl());
        if (request.getBannerUrl() != null) store.setBannerUrl(request.getBannerUrl());
        if (request.getBrandColor() != null) store.setBrandColor(request.getBrandColor());
        if (request.getTemplate() != null) {
            store.setTemplate(StoreTemplate.valueOf(request.getTemplate().toUpperCase()));
        }
        return toDto(storeRepository.save(store));
    }

    @CacheEvict(value = "store-public", key = "#storeId")
    public StoreDTO updateDomain(UUID storeId, StoreDomainRequest request) {
        Store store = findAccessibleStore(storeId);
        boolean allowed = subscriptionService.isFeatureEnabled(storeId, "custom_domain");
        if (!allowed) {
            throw new BusinessException("CUSTOM_DOMAIN_NOT_ALLOWED", "Your current plan does not allow custom domains.");
        }
        store.setCustomDomainName(request.getCustomDomain());
        return toDto(storeRepository.save(store));
    }

    @CacheEvict(value = "store-public", key = "#storeId")
    public StoreDTO updateSeo(UUID storeId, StoreSeoRequest request) {
        Store store = findAccessibleStore(storeId);
        if (request.getMetaTitle() != null)       store.setMetaTitle(request.getMetaTitle());
        if (request.getMetaDescription() != null) store.setMetaDescription(request.getMetaDescription());
        if (request.getKeywords() != null)         store.setKeywords(request.getKeywords());
        return toDto(storeRepository.save(store));
    }

    @Cacheable(value = "store-public", key = "#storeId")
    @Transactional(readOnly = true)
    public StoreDTO getPublicStoreById(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));
        if (store.getStoreStatus() == StoreStatus.SUSPENDED || Boolean.FALSE.equals(store.getIsActive())) {
            throw new ResourceNotFoundException("Store", "id", storeId);
        }
        return toDto(store);
    }

    /** Super-admin: return every active store regardless of tenant/auth context. */
    @Transactional(readOnly = true)
    public List<StoreDTO> getAllStores() {
        return storeRepository.findAllActive().stream().map(this::toDto).toList();
    }
}
