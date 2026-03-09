package com.cartwave.store.dto;

import lombok.Data;

@Data
public class StoreBrandingRequest {
    private String logoUrl;
    private String bannerUrl;
    /** Hex colour string, e.g. #1A73E8 */
    private String brandColor;
    /** One of: MINIMAL | MODERN | BOLD | CLASSIC */
    private String template;
}
