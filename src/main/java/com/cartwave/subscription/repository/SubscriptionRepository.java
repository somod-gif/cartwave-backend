package com.cartwave.subscription.repository;

import com.cartwave.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    @Query("SELECT s FROM Subscription s WHERE s.storeId = :storeId AND s.deleted = false")
    Optional<Subscription> findByStoreId(@Param("storeId") UUID storeId);

    @Query("SELECT s FROM Subscription s WHERE s.id = :id AND s.storeId = :storeId AND s.deleted = false")
    Optional<Subscription> findByIdAndStoreId(@Param("id") UUID id, @Param("storeId") UUID storeId);

    @Query("SELECT s FROM Subscription s WHERE s.deleted = false")
    List<Subscription> findAllActiveRecords();

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.deleted = false")
    List<Subscription> findByStatus(@Param("status") com.cartwave.subscription.entity.SubscriptionStatus status);

}
