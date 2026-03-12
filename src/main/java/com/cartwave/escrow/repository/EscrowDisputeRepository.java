package com.cartwave.escrow.repository;

import com.cartwave.escrow.entity.EscrowDispute;
import com.cartwave.escrow.entity.EscrowDisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EscrowDisputeRepository extends JpaRepository<EscrowDispute, UUID> {

    @Query("SELECT d FROM EscrowDispute d WHERE d.escrowTransactionId = :escrowId AND d.deleted = false ORDER BY d.createdAt DESC")
    List<EscrowDispute> findByEscrowTransactionId(@Param("escrowId") UUID escrowId);

    @Query("SELECT d FROM EscrowDispute d WHERE d.status = :status AND d.deleted = false ORDER BY d.createdAt DESC")
    List<EscrowDispute> findByStatus(@Param("status") EscrowDisputeStatus status);

    long countByStatusAndDeletedFalse(EscrowDisputeStatus status);
}
