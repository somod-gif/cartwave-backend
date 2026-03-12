package com.cartwave.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDTO {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String country;
    private String currency;
    private String subscriptionPlan;
    private Boolean isActive;
    private String logoUrl;
    private String websiteUrl;
    private Boolean customDomain;
    private String bannerUrl;
    private String businessAddress;
    private String businessRegistrationNumber;
    private String businessPhoneNumber;
    private String businessEmail;
    private UUID ownerId;
    private Instant createdAt;
    private Instant updatedAt;

    // ── V2 Store Builder fields ───────────────────────────────────────────────
    private String template;
    private String brandColor;
    private String customDomainName;
    private String subdomain;
    private String storeStatus;
    private String metaTitle;
    private String metaDescription;
    private String keywords;

}
