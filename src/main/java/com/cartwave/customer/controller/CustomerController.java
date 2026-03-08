package com.cartwave.customer.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.auth.dto.RegisterRequest;
import com.cartwave.auth.service.AuthService;
import com.cartwave.common.dto.ApiResponse;
import com.cartwave.customer.dto.CustomerProfileDTO;
import com.cartwave.customer.repository.CustomerRepository;
import com.cartwave.customer.service.CustomerService;
import com.cartwave.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final AuthService authService;
    private final CustomerRepository customerRepository;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerProfileDTO>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success("Customer profile retrieved successfully", customerService.getCurrentProfile()));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER','ADMIN','STAFF','SUPER_ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerProfileDTO>> getCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", customerService.getCustomerById(customerId)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerProfileDTO>> registerCustomer(@RequestBody RegisterRequest request) {
        request.setRole("CUSTOMER");
        var userDto = authService.register(request);
        var customer = customerRepository.findByUserIdAndStoreId(userDto.getId(), request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userId", userDto.getId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer registered successfully", customerService.getCustomerById(customer.getId())));
    }
}
