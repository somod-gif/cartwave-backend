package com.cartwave.customer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CustomerProfileDTO {

    private UUID id;
    private UUID userId;
    private UUID storeId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
}
