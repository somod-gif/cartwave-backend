package com.cartwave.order.service;

import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.order.dto.OrderDTO;
import com.cartwave.order.entity.Order;
import com.cartwave.order.mapper.OrderMapper;
import com.cartwave.order.repository.OrderRepository;
import com.cartwave.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(UUID orderId) {
        log.info("Fetching order by id: {}", orderId);
        UUID storeId = TenantContext.getTenantId();
        
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return orderMapper.toOrderDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByStore(Pageable pageable) {
        log.info("Fetching orders for store");
        UUID storeId = TenantContext.getTenantId();
        
        Page<Order> orders = orderRepository.findByStoreId(storeId, pageable);
        return orders.map(orderMapper::toOrderDTO);
    }

    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.info("Creating new order");
        UUID storeId = TenantContext.getTenantId();
        
        Order order = orderMapper.toOrder(orderDTO);
        order.setStoreId(storeId);
        
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toOrderDTO(savedOrder);
    }

    public OrderDTO updateOrder(UUID orderId, OrderDTO orderDTO) {
        log.info("Updating order: {}", orderId);
        UUID storeId = TenantContext.getTenantId();
        
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order = orderRepository.save(order);
        return orderMapper.toOrderDTO(order);
    }

}
