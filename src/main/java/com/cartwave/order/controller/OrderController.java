package com.cartwave.order.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.order.dto.OrderDTO;
import com.cartwave.order.dto.OrderStatusUpdateRequest;
import com.cartwave.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_OWNER', 'ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", orderService.getOrderById(orderId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_OWNER', 'ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> listOrders() {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orderService.getOrdersByStore()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", orderService.createOrder(orderDTO)));
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderDTO orderDTO
    ) {
        return ResponseEntity.ok(ApiResponse.success("Order updated successfully", orderService.updateOrder(orderId, orderDTO)));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", orderService.updateStatus(orderId, request.getStatus())));
    }
}
