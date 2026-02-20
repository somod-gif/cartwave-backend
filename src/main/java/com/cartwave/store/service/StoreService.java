package com.cartwave.store.service;

import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.store.dto.StoreDTO;
import com.cartwave.store.entity.Store;
import com.cartwave.store.mapper.StoreMapper;
import com.cartwave.store.repository.StoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
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

        // Update store fields
        store = storeRepository.save(store);
        return storeMapper.toStoreDTO(store);
    }

}
