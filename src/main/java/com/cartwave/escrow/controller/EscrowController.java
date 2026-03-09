package com.cartwave.escrow.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.escrow.dto.DisputeResolveRequest;
import com.cartwave.escrow.dto.EscrowDisputeDTO;
import com.cartwave.escrow.dto.EscrowDisputeRequest;
import com.cartwave.escrow.dto.EscrowTransactionDTO;
import com.cartwave.escrow.service.EscrowService;
import com.cartwave.security.service.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/escrow")
@RequiredArgsConstructor
@Tag(name = "Escrow", description = "Escrow payment hold, release, and dispute management")
public class EscrowController {

    private final EscrowService escrowService;
    private final CurrentUserService currentUserService;

    /**
     * Get all escrow transactions for a store.
     * Accessible by the store owner (BUSINESS_OWNER, ADMIN, SUPER_ADMIN).
     */
    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER','ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get escrow transactions for store")
    public ResponseEntity<ApiResponse<List<EscrowTransactionDTO>>> getStoreEscrow(
            @PathVariable UUID storeId) {
        List<EscrowTransactionDTO> result = escrowService.getStoreEscrow(storeId);
        return ResponseEntity.ok(ApiResponse.success("Escrow transactions fetched", result));
    }

    /**
     * Manually release an escrow hold. Admin only.
     */
    @PostMapping("/{escrowId}/release")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Manually release an escrow hold (Admin only)")
    public ResponseEntity<ApiResponse<EscrowTransactionDTO>> manualRelease(
            @PathVariable UUID escrowId) {
        EscrowTransactionDTO dto = escrowService.manualRelease(escrowId);
        return ResponseEntity.ok(ApiResponse.success("Escrow released successfully", dto));
    }

    /**
     * Buyer raises a dispute on an escrow transaction.
     */
    @PostMapping("/{escrowId}/dispute")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Raise a dispute on an escrow transaction")
    public ResponseEntity<ApiResponse<EscrowDisputeDTO>> raiseDispute(
            @PathVariable UUID escrowId,
            @Valid @RequestBody EscrowDisputeRequest request) {
        UUID userId = currentUserService.requireCurrentUserId();
        EscrowDisputeDTO dto = escrowService.raiseDispute(escrowId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Dispute raised successfully", dto));
    }

    /**
     * Admin resolves a dispute.
     */
    @PutMapping("/dispute/{disputeId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Resolve a dispute (Admin only)")
    public ResponseEntity<ApiResponse<EscrowDisputeDTO>> resolveDispute(
            @PathVariable UUID disputeId,
            @Valid @RequestBody DisputeResolveRequest request) {
        EscrowDisputeDTO dto = escrowService.resolveDispute(disputeId, request);
        return ResponseEntity.ok(ApiResponse.success("Dispute resolved", dto));
    }
}
