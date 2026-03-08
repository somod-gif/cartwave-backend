package com.cartwave.order.repository;

import com.cartwave.order.entity.Order;
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
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.storeId = :storeId AND o.deleted = false")
    Optional<Order> findByIdAndStoreId(@Param("id") UUID id, @Param("storeId") UUID storeId);

    @Query("SELECT o FROM Order o WHERE o.storeId = :storeId AND o.deleted = false")
    Page<Order> findByStoreId(@Param("storeId") UUID storeId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber AND o.storeId = :storeId AND o.deleted = false")
    Optional<Order> findByOrderNumberAndStoreId(@Param("orderNumber") String orderNumber, @Param("storeId") UUID storeId);

    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.storeId = :storeId AND o.deleted = false ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdAndStoreId(@Param("customerId") UUID customerId, @Param("storeId") UUID storeId);

    long countByStoreIdAndDeletedFalse(UUID storeId);

    long countByStatusAndStoreIdAndDeletedFalse(com.cartwave.order.entity.OrderStatus status, UUID storeId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.storeId = :storeId AND o.deleted = false AND o.paymentStatus IN ('COMPLETED', 'PARTIALLY_REFUNDED')")
    BigDecimal sumRevenueForStore(@Param("storeId") UUID storeId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.deleted = false AND o.paymentStatus IN ('COMPLETED', 'PARTIALLY_REFUNDED')")
    BigDecimal sumRevenueGlobal();

    long countByDeletedFalse();

}
