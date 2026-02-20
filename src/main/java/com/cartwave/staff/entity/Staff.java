package com.cartwave.staff.entity;

import com.cartwave.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "staff", indexes = {
        @Index(name = "idx_staff_store_id", columnList = "store_id"),
        @Index(name = "idx_staff_user_id", columnList = "user_id"),
        @Index(name = "idx_staff_deleted", columnList = "deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Staff extends BaseEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID storeId;

    @Column(length = 50)
    private String permissionLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StaffRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StaffStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private Long hiredAt;

}
