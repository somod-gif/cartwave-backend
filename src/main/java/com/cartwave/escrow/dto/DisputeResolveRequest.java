package com.cartwave.escrow.dto;

import lombok.Data;

@Data
public class DisputeResolveRequest {
    private String resolutionNotes;
    private String adminResolutionNotes;
    /** RESOLVED | REJECTED */
    private String status;
}
