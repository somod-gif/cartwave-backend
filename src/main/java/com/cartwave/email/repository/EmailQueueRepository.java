package com.cartwave.email.repository;

import com.cartwave.email.entity.EmailQueue;
import com.cartwave.email.entity.EmailStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmailQueueRepository extends JpaRepository<EmailQueue, UUID> {
    List<EmailQueue> findByStatusOrderByCreatedAtAsc(EmailStatus status, Pageable pageable);

    long countByStatus(EmailStatus status);
}
