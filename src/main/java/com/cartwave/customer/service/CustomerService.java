package com.cartwave.customer.service;

import com.cartwave.customer.dto.CustomerProfileDTO;
import com.cartwave.customer.entity.Customer;
import com.cartwave.customer.repository.CustomerRepository;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.security.model.CurrentUserPrincipal;
import com.cartwave.security.service.CurrentUserService;
import com.cartwave.tenant.TenantContext;
import com.cartwave.user.entity.User;
import com.cartwave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Customer requireCurrentCustomer() {
        CurrentUserPrincipal principal = currentUserService.requireCurrentUser();
        var storeId = TenantContext.getTenantId();
        return customerRepository.findByUserIdAndStoreId(principal.getUserId(), storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "storeId", storeId));
    }

    @Transactional(readOnly = true)
    public CustomerProfileDTO getCurrentProfile() {
        Customer customer = requireCurrentCustomer();
        User user = userRepository.findById(customer.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customer.getUserId()));
        return toProfile(customer, user);
    }

    @Transactional(readOnly = true)
    public CustomerProfileDTO getCustomerById(UUID customerId) {
        UUID storeId = TenantContext.getTenantId();
        Customer customer = customerRepository.findByIdAndStoreId(customerId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        User user = userRepository.findById(customer.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customer.getUserId()));
        return toProfile(customer, user);
    }

    private CustomerProfileDTO toProfile(Customer customer, User user) {
        return CustomerProfileDTO.builder()
                .id(customer.getId())
                .userId(customer.getUserId())
                .storeId(customer.getStoreId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(customer.getPhone())
                .build();
    }
}
