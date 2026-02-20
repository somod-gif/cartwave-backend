package com.cartwave.store.repository;

import com.cartwave.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    @Query("SELECT s FROM Store s WHERE s.slug = :slug AND s.deleted = false")
    Optional<Store> findBySlug(@Param("slug") String slug);

    @Query("SELECT s FROM Store s WHERE s.id = :id AND s.deleted = false")
    @Override
    Optional<Store> findById(UUID id);

    @Query("SELECT s FROM Store s WHERE s.ownerId = :ownerId AND s.deleted = false")
    Optional<Store> findByOwnerId(@Param("ownerId") UUID ownerId);

}
