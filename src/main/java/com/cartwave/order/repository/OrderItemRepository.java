package com.cartwave.order.repository;

import com.cartwave.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId AND oi.deleted = false")
    List<OrderItem> findByOrderId(@Param("orderId") UUID orderId);

    @Query("""
        SELECT COUNT(oi) > 0 FROM OrderItem oi
        JOIN Order o ON o.id = oi.orderId
        WHERE oi.productId = :productId
          AND o.customerId = :customerId
          AND oi.deleted = false
          AND o.deleted = false
        """)
    boolean existsPurchaseByCustomer(@Param("productId") UUID productId, @Param("customerId") UUID customerId);
}
