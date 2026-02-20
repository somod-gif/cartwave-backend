package com.cartwave.store.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.store.dto.StoreDTO;
import com.cartwave.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<StoreDTO>> getStore(@PathVariable UUID storeId) {
        log.info("Get store endpoint called");
        StoreDTO storeDTO = storeService.getStoreById(storeId);
        return ResponseEntity.ok(ApiResponse.success("Store retrieved successfully", storeDTO));
    }

    @PutMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StoreDTO>> updateStore(
            @PathVariable UUID storeId,
            @RequestBody StoreDTO storeDTO) {
        log.info("Update store endpoint called");
        StoreDTO updatedStore = storeService.updateStore(storeId, storeDTO);
        return ResponseEntity.ok(ApiResponse.success("Store updated successfully", updatedStore));
    }

}
