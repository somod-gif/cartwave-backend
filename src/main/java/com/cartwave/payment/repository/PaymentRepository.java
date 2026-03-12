package com.cartwave.payment.repository;

import com.cartwave.payment.entity.Payment;
import com.cartwave.order.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId AND p.deleted = false")
    Optional<Payment> findByTransactionId(@Param("transactionId") String transactionId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.deleted = false")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.deleted = false AND p.createdAt >= :since")
    BigDecimal sumAmountByStatusSince(@Param("status") PaymentStatus status, @Param("since") java.time.Instant since);

    long countByDeletedFalse();

    List<Payment> findAllByStoreIdAndDeletedFalseOrderByCreatedAtDesc(UUID storeId);
}
