package com.cartwave.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    // allow toggling custom domain from UI
    private Boolean customDomain;

}
