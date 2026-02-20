package com.cartwave.order.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.order.dto.OrderDTO;
import com.cartwave.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(@PathVariable UUID orderId) {
        log.info("Get order endpoint called");
        OrderDTO orderDTO = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", orderDTO));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> listOrders(Pageable pageable) {
        log.info("List orders endpoint called");
        Page<OrderDTO> orders = orderService.getOrdersByStore(pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        log.info("Create order endpoint called");
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", createdOrder));
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderDTO orderDTO) {
        log.info("Update order endpoint called");
        OrderDTO updatedOrder = orderService.updateOrder(orderId, orderDTO);
        return ResponseEntity.ok(ApiResponse.success("Order updated successfully", updatedOrder));
    }

}
