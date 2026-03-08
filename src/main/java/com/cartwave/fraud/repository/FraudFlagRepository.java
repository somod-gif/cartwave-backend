package com.cartwave.fraud.repository;

import com.cartwave.fraud.entity.FraudFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FraudFlagRepository extends JpaRepository<FraudFlag, UUID> {

    long countByStoreIdAndReviewedFalseAndDeletedFalse(UUID storeId);

    boolean existsByOrderIdAndReasonAndDeletedFalse(UUID orderId, String reason);
}
