package com.cartwave.escrow.dto;

import lombok.Data;

@Data
public class EscrowDisputeRequest {
    /** Reason for raising the dispute */
    private String reason;
    /** Supporting evidence text */
    private String evidence;
}
