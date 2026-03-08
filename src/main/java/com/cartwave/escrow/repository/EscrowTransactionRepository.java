package com.cartwave.escrow.repository;

import com.cartwave.escrow.entity.EscrowStatus;
import com.cartwave.escrow.entity.EscrowTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EscrowTransactionRepository extends JpaRepository<EscrowTransaction, UUID> {

    @Query("SELECT e FROM EscrowTransaction e WHERE e.orderId = :orderId AND e.deleted = false")
    Optional<EscrowTransaction> findByOrderId(@Param("orderId") UUID orderId);

    @Query("SELECT e FROM EscrowTransaction e WHERE e.status = :status AND e.releaseAt IS NOT NULL AND e.releaseAt <= :now AND e.deleted = false")
    List<EscrowTransaction> findReleasable(@Param("status") EscrowStatus status, @Param("now") long now);

    long countByStatusAndDeletedFalse(EscrowStatus status);

    long countByStoreIdAndStatusAndDeletedFalse(UUID storeId, EscrowStatus status);
}
