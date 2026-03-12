package com.cartwave.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserAdminDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String role;
    private String status;
    private Boolean emailVerified;
    private Long lastLoginAt;
    private Instant createdAt;
}
