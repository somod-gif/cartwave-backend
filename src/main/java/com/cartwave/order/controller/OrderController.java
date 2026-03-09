package com.cartwave.order.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.order.dto.OrderDTO;
import com.cartwave.order.dto.OrderStatusUpdateRequest;
import com.cartwave.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Orders", description = "Order management endpoints")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Get order by ID")
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_OWNER', 'ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", orderService.getOrderById(orderId)));
    }

    @Operation(summary = "List orders for the current tenant/store")
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_OWNER', 'ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> listOrders() {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orderService.getOrdersByStore()));
    }

    @Operation(summary = "Create an order")
    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", orderService.createOrder(orderDTO)));
    }

    @Operation(summary = "Update order details")
    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderDTO orderDTO
    ) {
        return ResponseEntity.ok(ApiResponse.success("Order updated successfully", orderService.updateOrder(orderId, orderDTO)));
    }

    @Operation(summary = "Update order status (PATCH)")
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", orderService.updateStatus(orderId, request.getStatus())));
    }

    @Operation(summary = "Update order status (PUT, alias)")
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatusPut(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", orderService.updateStatus(orderId, request.getStatus())));
    }

    @Operation(summary = "Get all orders for a store (BUSINESS_OWNER / ADMIN)")
    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByStore(@PathVariable UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orderService.getOrdersByStoreId(storeId)));
    }

    @Operation(summary = "Get all orders for a customer")
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_OWNER', 'ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orderService.getOrdersByCustomerId(customerId)));
    }
}
