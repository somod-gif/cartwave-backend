package com.cartwave.staff.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class StaffDTO {

    private UUID id;
    private UUID userId;
    private UUID storeId;
    private String role;
    private String status;
    private String permissionLevel;
    private String notes;
    private Long hiredAt;
    private Instant createdAt;
}
