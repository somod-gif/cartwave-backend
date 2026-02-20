package com.cartwave.order.repository;

import com.cartwave.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.storeId = :storeId AND o.deleted = false")
    Optional<Order> findByIdAndStoreId(@Param("id") UUID id, @Param("storeId") UUID storeId);

    @Query("SELECT o FROM Order o WHERE o.storeId = :storeId AND o.deleted = false")
    Page<Order> findByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber AND o.storeId = :storeId AND o.deleted = false")
    Optional<Order> findByOrderNumberAndStoreId(@Param("orderNumber") String orderNumber, @Param("storeId") UUID storeId);

}
