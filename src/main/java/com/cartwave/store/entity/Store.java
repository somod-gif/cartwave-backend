package com.cartwave.store.entity;

import com.cartwave.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "stores", indexes = {
        @Index(name = "idx_stores_slug", columnList = "slug"),
        @Index(name = "idx_stores_owner_id", columnList = "owner_id"),
        @Index(name = "idx_stores_is_active", columnList = "is_active"),
        @Index(name = "idx_stores_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Store extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String country;

    @Column(length = 10)
    private String currency;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(length = 50)
    private String subscriptionPlan;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 500)
    private String bannerUrl;

    @Column(length = 500)
    private String websiteUrl;

    @Column(columnDefinition = "TEXT")
    private String businessAddress;

    @Column(length = 50)
    private String businessRegistrationNumber;

    @Column(length = 20)
    private String businessPhoneNumber;

    @Column(length = 255)
    private String businessEmail;

}
