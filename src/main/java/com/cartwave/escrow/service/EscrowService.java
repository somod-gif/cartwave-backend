package com.cartwave.escrow.service;

import com.cartwave.escrow.entity.EscrowDispute;
import com.cartwave.escrow.entity.EscrowDisputeStatus;
import com.cartwave.escrow.entity.EscrowStatus;
import com.cartwave.escrow.entity.EscrowTransaction;
import com.cartwave.escrow.repository.EscrowDisputeRepository;
import com.cartwave.escrow.repository.EscrowTransactionRepository;
import com.cartwave.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EscrowService {

    private final EscrowTransactionRepository escrowTransactionRepository;
    private final EscrowDisputeRepository escrowDisputeRepository;

    public EscrowTransaction createOrUpdateHold(UUID storeId, UUID orderId, BigDecimal amount, Long releaseAt, String reference) {
        EscrowTransaction tx = escrowTransactionRepository.findByOrderId(orderId).orElseGet(() -> EscrowTransaction.builder()
                .storeId(storeId)
                .orderId(orderId)
                .build());
        tx.setHoldAmount(amount);
        tx.setStatus(EscrowStatus.HELD);
        tx.setReleaseAt(releaseAt);
        tx.setTransactionRef(reference);
        return escrowTransactionRepository.save(tx);
    }

    public EscrowTransaction markReleased(UUID orderId) {
        EscrowTransaction tx = escrowTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("ESCROW_NOT_FOUND", "No escrow record for order."));
        tx.setStatus(EscrowStatus.RELEASED);
        tx.setReleaseAt(Instant.now().toEpochMilli());
        return escrowTransactionRepository.save(tx);
    }

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

    public void processReleasable(long now) {
        escrowTransactionRepository.findReleasable(EscrowStatus.HELD, now).forEach(tx -> {
            tx.setStatus(EscrowStatus.RELEASED);
            tx.setReleaseAt(now);
            escrowTransactionRepository.save(tx);
        });
    }
}
