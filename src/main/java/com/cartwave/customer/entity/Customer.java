package com.cartwave.customer.entity;

import com.cartwave.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_user_store", columnList = "user_id,store_id"),
        @Index(name = "idx_customers_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID storeId;

    @Column(length = 64)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String addressesJson;

    @Column(columnDefinition = "TEXT")
    private String wishlistJson;
}
