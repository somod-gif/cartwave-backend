package com.cartwave.store.dto;

import lombok.Data;

@Data
public class StoreDomainRequest {
    /** Fully-qualified custom domain, e.g. www.mybrand.com */
    private String customDomain;
}
