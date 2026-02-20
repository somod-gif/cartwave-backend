package com.cartwave.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {

    private UUID userId;
    private String email;
    private String role;
    private UUID storeId;
    private String tokenType;
    private List<String> permissions;

}
