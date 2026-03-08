package com.cartwave.cart.repository;

import com.cartwave.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId AND ci.deleted = false ORDER BY ci.createdAt ASC")
    List<CartItem> findByCartId(@Param("cartId") UUID cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId AND ci.productId = :productId AND ci.deleted = false")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") UUID cartId, @Param("productId") UUID productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.id = :id AND ci.cartId = :cartId AND ci.deleted = false")
    Optional<CartItem> findByIdAndCartId(@Param("id") UUID id, @Param("cartId") UUID cartId);
}
