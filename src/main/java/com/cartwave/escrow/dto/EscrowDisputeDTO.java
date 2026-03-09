package com.cartwave.escrow.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class EscrowDisputeDTO {
    private UUID id;
    private UUID escrowTransactionId;
    private UUID raisedByUserId;
    private String reason;
    private String evidence;
    private String status;
    private String resolutionNotes;
    private String adminResolutionNotes;
    private Long resolvedAt;
    private Instant createdAt;
}
