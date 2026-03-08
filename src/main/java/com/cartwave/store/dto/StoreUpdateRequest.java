package com.cartwave.store.dto;

import lombok.Data;

@Data
public class StoreUpdateRequest {

    private String name;
    private String description;
    private String country;
    private String currency;
    private String subscriptionPlan;
    private Boolean isActive;
    private String logoUrl;
    private String bannerUrl;
    private String websiteUrl;
    private String businessAddress;
    private String businessRegistrationNumber;
    private String businessPhoneNumber;
    private String businessEmail;
    private Boolean customDomain;
}
