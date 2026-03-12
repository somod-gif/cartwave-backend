package com.cartwave.order.repository;

import com.cartwave.order.entity.OrderTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderTrackingRepository extends JpaRepository<OrderTracking, UUID> {

    List<OrderTracking> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
