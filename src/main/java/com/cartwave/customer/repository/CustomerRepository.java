package com.cartwave.customer.repository;

import com.cartwave.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Query("SELECT c FROM Customer c WHERE c.userId = :userId AND c.storeId = :storeId AND c.deleted = false")
    Optional<Customer> findByUserIdAndStoreId(@Param("userId") UUID userId, @Param("storeId") UUID storeId);

    @Query("SELECT c FROM Customer c WHERE c.id = :id AND c.storeId = :storeId AND c.deleted = false")
    Optional<Customer> findByIdAndStoreId(@Param("id") UUID id, @Param("storeId") UUID storeId);

    @Query("SELECT c.storeId FROM Customer c WHERE c.userId = :userId AND c.deleted = false")
    List<UUID> findStoreIdsByUserId(@Param("userId") UUID userId);

    long countByStoreIdAndDeletedFalse(UUID storeId);

    long countByDeletedFalse();
}
