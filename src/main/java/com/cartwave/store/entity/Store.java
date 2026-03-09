package com.cartwave.store.entity;

import com.cartwave.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "stores", indexes = {
        @Index(name = "idx_stores_slug", columnList = "slug"),
        @Index(name = "idx_stores_owner_id", columnList = "owner_user_id"),
        @Index(name = "idx_stores_is_active", columnList = "active"),
        @Index(name = "idx_stores_deleted", columnList = "deleted"),
        @Index(name = "idx_stores_subdomain", columnList = "subdomain"),
        @Index(name = "idx_stores_store_status", columnList = "store_status")
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

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerId;

    @Column(length = 50)
    private String subscriptionPlan;

    @Builder.Default
    @Column(name = "active", nullable = false)
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

    // ── V2 Store Builder fields ───────────────────────────────────────────────

    /** UI template preference */
    @Enumerated(EnumType.STRING)
    @Column(name = "template", length = 20)
    private StoreTemplate template;

    /** Primary brand colour, hex string e.g. #1A73E8 */
    @Column(name = "brand_color", length = 20)
    private String brandColor;

    /** Custom domain e.g. www.mybrand.com (requires plan feature) */
    @Column(name = "custom_domain_name", length = 255)
    private String customDomainName;

    /** Auto-generated subdomain e.g. mybrand.cartwave.store */
    @Column(name = "subdomain", length = 255, unique = true)
    private String subdomain;

    /** Lifecycle status — separate from the legacy isActive flag */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "store_status", length = 20, nullable = false)
    private StoreStatus storeStatus = StoreStatus.ACTIVE;

    // ── SEO ──────────────────────────────────────────────────────────────────

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    /** Comma-separated keyword list */
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

}
