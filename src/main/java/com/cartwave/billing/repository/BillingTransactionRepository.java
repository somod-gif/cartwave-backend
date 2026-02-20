package com.cartwave.billing.repository;

import com.cartwave.billing.entity.BillingTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillingTransactionRepository extends JpaRepository<BillingTransaction, UUID> {

    @Query("SELECT b FROM BillingTransaction b WHERE b.storeId = :storeId AND b.deleted = false")
    Page<BillingTransaction> findByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

    @Query("SELECT b FROM BillingTransaction b WHERE b.transactionId = :transactionId AND b.storeId = :storeId AND b.deleted = false")
    Optional<BillingTransaction> findByTransactionIdAndStoreId(@Param("transactionId") String transactionId, @Param("storeId") UUID storeId);

}
