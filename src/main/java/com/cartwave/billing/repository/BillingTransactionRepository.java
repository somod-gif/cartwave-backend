package com.cartwave.billing.repository;

import com.cartwave.billing.entity.BillingTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillingTransactionRepository extends JpaRepository<BillingTransaction, UUID> {

    @Query("SELECT b FROM BillingTransaction b WHERE b.storeId = :storeId AND b.deleted = false")
    Page<BillingTransaction> findByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

    @Query("SELECT b FROM BillingTransaction b WHERE b.transactionId = :transactionId AND b.storeId = :storeId AND b.deleted = false")
    Optional<BillingTransaction> findByTransactionIdAndStoreId(@Param("transactionId") String transactionId, @Param("storeId") UUID storeId);

    @Query("SELECT b FROM BillingTransaction b WHERE b.transactionId = :transactionId AND b.deleted = false")
    Optional<BillingTransaction> findByTransactionId(@Param("transactionId") String transactionId);

    @Query("SELECT b FROM BillingTransaction b WHERE b.orderId = :orderId AND b.storeId = :storeId AND b.deleted = false ORDER BY b.createdAt DESC")
    Optional<BillingTransaction> findFirstByOrderIdAndStoreId(@Param("orderId") UUID orderId, @Param("storeId") UUID storeId);

    @Query("SELECT b FROM BillingTransaction b WHERE b.status = :status AND b.releaseAt IS NOT NULL AND b.releaseAt <= :releaseAt AND b.deleted = false")
    List<BillingTransaction> findReleasable(
            @Param("status") com.cartwave.billing.entity.BillingStatus status,
            @Param("releaseAt") Long releaseAt
    );

    long countByStoreIdAndStatusAndDeletedFalse(UUID storeId, com.cartwave.billing.entity.BillingStatus status);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM BillingTransaction b WHERE b.storeId = :storeId AND b.deleted = false AND b.status IN ('HOLD', 'RELEASED', 'COMPLETED')")
    BigDecimal sumCapturedForStore(@Param("storeId") UUID storeId);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM BillingTransaction b WHERE b.deleted = false AND b.status IN ('HOLD', 'RELEASED', 'COMPLETED')")
    BigDecimal sumCapturedGlobal();

    long countByDeletedFalse();

}
