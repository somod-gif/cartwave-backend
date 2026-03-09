package com.cartwave.escrow.service;

import com.cartwave.email.service.EmailQueueService;
import com.cartwave.escrow.dto.DisputeResolveRequest;
import com.cartwave.escrow.dto.EscrowDisputeDTO;
import com.cartwave.escrow.dto.EscrowDisputeRequest;
import com.cartwave.escrow.dto.EscrowTransactionDTO;
import com.cartwave.escrow.entity.EscrowDispute;
import com.cartwave.escrow.entity.EscrowDisputeStatus;
import com.cartwave.escrow.entity.EscrowStatus;
import com.cartwave.escrow.entity.EscrowTransaction;
import com.cartwave.escrow.repository.EscrowDisputeRepository;
import com.cartwave.escrow.repository.EscrowTransactionRepository;
import com.cartwave.exception.BusinessException;
import com.cartwave.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EscrowService {

    private static final BigDecimal DEFAULT_PLATFORM_FEE_PERCENT = new BigDecimal("2.50");

    private final EscrowTransactionRepository escrowTransactionRepository;
    private final EscrowDisputeRepository escrowDisputeRepository;
    private final EmailQueueService emailQueueService;

    public EscrowTransaction createOrUpdateHold(UUID storeId, UUID orderId, BigDecimal amount, Long releaseAt, String reference) {
        EscrowTransaction tx = escrowTransactionRepository.findByOrderId(orderId).orElseGet(() -> EscrowTransaction.builder()
                .storeId(storeId)
                .orderId(orderId)
                .build());
        tx.setHoldAmount(amount);
        tx.setStatus(EscrowStatus.HELD);
        tx.setReleaseAt(releaseAt);
        tx.setTransactionRef(reference);
        tx.setPlatformFeePercent(DEFAULT_PLATFORM_FEE_PERCENT);
        tx.setSellerAmount(computeSellerAmount(amount, DEFAULT_PLATFORM_FEE_PERCENT));
        return escrowTransactionRepository.save(tx);
    }

    public EscrowTransaction markReleased(UUID orderId) {
        EscrowTransaction tx = escrowTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("ESCROW_NOT_FOUND", "No escrow record for order."));
        tx.setStatus(EscrowStatus.RELEASED);
        tx.setReleasedAt(Instant.now().toEpochMilli());
        EscrowTransaction saved = escrowTransactionRepository.save(tx);
        emailQueueService.enqueueEscrowReleased(saved.getStoreId(), saved.getId(), saved.getSellerAmount());
        return saved;
    }

    /** Manual release by ADMIN */
    public EscrowTransactionDTO manualRelease(UUID escrowId) {
        EscrowTransaction tx = escrowTransactionRepository.findById(escrowId)
                .orElseThrow(() -> new ResourceNotFoundException("EscrowTransaction", "id", escrowId));
        if (tx.getStatus() == EscrowStatus.RELEASED) {
            throw new BusinessException("ESCROW_ALREADY_RELEASED", "Escrow is already released.");
        }
        tx.setStatus(EscrowStatus.RELEASED);
        tx.setReleasedAt(Instant.now().toEpochMilli());
        EscrowTransaction saved = escrowTransactionRepository.save(tx);
        emailQueueService.enqueueEscrowReleased(saved.getStoreId(), saved.getId(), saved.getSellerAmount());
        return toDto(saved);
    }

    /** Buyer raises a dispute */
    public EscrowDisputeDTO raiseDispute(UUID escrowId, UUID userId, EscrowDisputeRequest request) {
        EscrowTransaction tx = escrowTransactionRepository.findById(escrowId)
                .orElseThrow(() -> new ResourceNotFoundException("EscrowTransaction", "id", escrowId));
        tx.setStatus(EscrowStatus.DISPUTED);
        escrowTransactionRepository.save(tx);

        EscrowDispute dispute = EscrowDispute.builder()
                .escrowTransactionId(tx.getId())
                .raisedByUserId(userId)
                .reason(request.getReason())
                .evidence(request.getEvidence())
                .status(EscrowDisputeStatus.OPEN)
                .build();
        EscrowDispute saved = escrowDisputeRepository.save(dispute);
        emailQueueService.enqueueDisputeOpened(tx.getStoreId(), escrowId, userId);
        return toDisputeDto(saved);
    }

    /** Legacy dispute method kept for backward compat */
    public EscrowTransaction dispute(UUID escrowId, UUID userId, String reason) {
        EscrowTransaction tx = escrowTransactionRepository.findById(escrowId)
                .orElseThrow(() -> new BusinessException("ESCROW_NOT_FOUND", "Escrow transaction not found."));
        tx.setStatus(EscrowStatus.DISPUTED);
        escrowTransactionRepository.save(tx);

        EscrowDispute dispute = EscrowDispute.builder()
                .escrowTransactionId(tx.getId())
                .raisedByUserId(userId)
                .reason(reason)
                .status(EscrowDisputeStatus.OPEN)
                .build();
        escrowDisputeRepository.save(dispute);
        return tx;
    }

    /** Admin resolves a dispute */
    public EscrowDisputeDTO resolveDispute(UUID disputeId, DisputeResolveRequest request) {
        EscrowDispute dispute = escrowDisputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("EscrowDispute", "id", disputeId));

        EscrowDisputeStatus newStatus = request.getStatus() != null
                ? EscrowDisputeStatus.valueOf(request.getStatus().toUpperCase())
                : EscrowDisputeStatus.RESOLVED;
        dispute.setStatus(newStatus);
        dispute.setResolutionNotes(request.getResolutionNotes());
        dispute.setAdminResolutionNotes(request.getAdminResolutionNotes());
        dispute.setResolvedAt(Instant.now().toEpochMilli());

        EscrowTransaction tx = escrowTransactionRepository.findById(dispute.getEscrowTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("EscrowTransaction", "id", dispute.getEscrowTransactionId()));

        if (newStatus == EscrowDisputeStatus.RESOLVED) {
            tx.setStatus(EscrowStatus.RELEASED);
            tx.setReleasedAt(Instant.now().toEpochMilli());
            escrowTransactionRepository.save(tx);
        }

        EscrowDispute saved = escrowDisputeRepository.save(dispute);
        emailQueueService.enqueueDisputeResolved(tx.getStoreId(), disputeId, dispute.getRaisedByUserId());
        return toDisputeDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EscrowTransactionDTO> getStoreEscrow(UUID storeId) {
        return escrowTransactionRepository.findAllByStoreId(storeId).stream().map(this::toDto).toList();
    }

    public void processReleasable(long now) {
        escrowTransactionRepository.findReleasable(EscrowStatus.HELD, now).forEach(tx -> {
            tx.setStatus(EscrowStatus.RELEASED);
            tx.setReleasedAt(now);
            escrowTransactionRepository.save(tx);
            try {
                emailQueueService.enqueueEscrowReleased(tx.getStoreId(), tx.getId(), tx.getSellerAmount());
            } catch (Exception e) {
                log.warn("Failed to enqueue escrow released email for tx {}: {}", tx.getId(), e.getMessage());
            }
        });
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private BigDecimal computeSellerAmount(BigDecimal holdAmount, BigDecimal feePercent) {
        if (holdAmount == null || feePercent == null) return holdAmount;
        BigDecimal fee = holdAmount.multiply(feePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return holdAmount.subtract(fee);
    }

    public EscrowTransactionDTO toDto(EscrowTransaction tx) {
        return EscrowTransactionDTO.builder()
                .id(tx.getId())
                .storeId(tx.getStoreId())
                .orderId(tx.getOrderId())
                .holdAmount(tx.getHoldAmount())
                .platformFeePercent(tx.getPlatformFeePercent())
                .sellerAmount(tx.getSellerAmount())
                .status(tx.getStatus() == null ? null : tx.getStatus().name())
                .releaseAt(tx.getReleaseAt())
                .releasedAt(tx.getReleasedAt())
                .transactionRef(tx.getTransactionRef())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    public EscrowDisputeDTO toDisputeDto(EscrowDispute d) {
        return EscrowDisputeDTO.builder()
                .id(d.getId())
                .escrowTransactionId(d.getEscrowTransactionId())
                .raisedByUserId(d.getRaisedByUserId())
                .reason(d.getReason())
                .evidence(d.getEvidence())
                .status(d.getStatus() == null ? null : d.getStatus().name())
                .resolutionNotes(d.getResolutionNotes())
                .adminResolutionNotes(d.getAdminResolutionNotes())
                .resolvedAt(d.getResolvedAt())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
