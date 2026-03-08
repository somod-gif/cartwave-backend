package com.cartwave.analytics.repository;

import com.cartwave.analytics.entity.KpiSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KpiSnapshotRepository extends JpaRepository<KpiSnapshot, UUID> {

    Optional<KpiSnapshot> findByStoreIdAndSnapshotDateAndDeletedFalse(UUID storeId, LocalDate snapshotDate);
}
