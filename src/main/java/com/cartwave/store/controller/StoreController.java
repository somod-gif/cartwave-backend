package com.cartwave.store.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.store.dto.StoreCreateRequest;
import com.cartwave.store.dto.StoreDTO;
import com.cartwave.store.dto.StoreUpdateRequest;
import com.cartwave.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<StoreDTO>>> listStores() {
        return ResponseEntity.ok(ApiResponse.success("Stores retrieved successfully", storeService.listStores()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StoreDTO>> createStore(@Valid @RequestBody StoreCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Store created successfully", storeService.createStore(request)));
    }

    @GetMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<StoreDTO>> getStore(@PathVariable UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success("Store retrieved successfully", storeService.getStoreById(storeId)));
    }

    @PutMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StoreDTO>> updateStore(
            @PathVariable UUID storeId,
            @RequestBody StoreUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Store updated successfully", storeService.updateStore(storeId, request)));
    }

    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable UUID storeId) {
        storeService.deleteStore(storeId);
        return ResponseEntity.ok(ApiResponse.success("Store deleted successfully", null));
    }
}
