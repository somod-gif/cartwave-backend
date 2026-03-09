package com.cartwave.store.dto;

import lombok.Data;

@Data
public class StoreSeoRequest {
    private String metaTitle;
    private String metaDescription;
    /** Comma-separated keyword list */
    private String keywords;
}
