package com.revpay.repository;

import com.revpay.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByPerformedById(Long userId, Pageable pageable);

    Page<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);

    List<AuditLog> findByAction(String action);

    Page<AuditLog> findByCreatedAtBetween(Instant from, Instant to, Pageable pageable);
}