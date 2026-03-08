package com.cartwave.escrow.repository;

import com.cartwave.escrow.entity.EscrowDispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EscrowDisputeRepository extends JpaRepository<EscrowDispute, UUID> {
}
