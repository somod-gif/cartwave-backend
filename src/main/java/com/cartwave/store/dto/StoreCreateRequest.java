package com.cartwave.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private String description;
    private String country;
    private String currency;
    private String logoUrl;
    private String bannerUrl;
    private String websiteUrl;
    private String businessAddress;
    private String businessRegistrationNumber;
    private String businessPhoneNumber;
    private String businessEmail;
}
