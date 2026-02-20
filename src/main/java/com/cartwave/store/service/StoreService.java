package com.cartwave.store.service;

import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.exception.BusinessException;
import com.cartwave.store.dto.StoreDTO;
import com.cartwave.store.entity.Store;
import com.cartwave.store.mapper.StoreMapper;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.subscription.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final SubscriptionService subscriptionService;

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper, SubscriptionService subscriptionService) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
        this.subscriptionService = subscriptionService;
    }

    @Transactional(readOnly = true)
    public StoreDTO getStoreById(UUID storeId) {
        log.info("Fetching store by id: {}", storeId);
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));

        return storeMapper.toStoreDTO(store);
    }

    public StoreDTO updateStore(UUID storeId, StoreDTO storeDTO) {
        log.info("Updating store: {}", storeId);
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));

        // Enforce custom domain availability
        if (Boolean.TRUE.equals(storeDTO.getCustomDomain())) {
            boolean allowed = subscriptionService.isFeatureEnabled(storeId, "custom_domain");
            if (!allowed) {
                throw new BusinessException("CUSTOM_DOMAIN_NOT_ALLOWED", "Current subscription plan does not allow custom domains.");
            }
        }

        // Update store fields
        store = storeRepository.save(store);
        return storeMapper.toStoreDTO(store);
    }

}
