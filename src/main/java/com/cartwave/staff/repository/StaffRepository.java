package com.cartwave.staff.repository;

import com.cartwave.staff.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {

    @Query("SELECT s FROM Staff s WHERE s.userId = :userId AND s.storeId = :storeId AND s.deleted = false")
    Optional<Staff> findByUserIdAndStoreId(@Param("userId") UUID userId, @Param("storeId") UUID storeId);

    @Query("SELECT s FROM Staff s WHERE s.id = :id AND s.storeId = :storeId AND s.deleted = false")
    Optional<Staff> findByIdAndStoreId(@Param("id") UUID id, @Param("storeId") UUID storeId);

}
