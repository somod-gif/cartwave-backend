package com.cartwave.marketing.repository;

import com.cartwave.marketing.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    List<Coupon> findAllByStoreIdAndDeletedFalse(UUID storeId);

    Optional<Coupon> findByStoreIdAndCodeIgnoreCaseAndDeletedFalse(UUID storeId, String code);

    boolean existsByStoreIdAndCodeIgnoreCaseAndDeletedFalse(UUID storeId, String code);
}
