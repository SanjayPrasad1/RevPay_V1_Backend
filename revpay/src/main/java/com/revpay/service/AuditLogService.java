package com.revpay.service;

import com.revpay.entity.AuditLog;
import com.revpay.entity.User;
import com.revpay.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String entityName,
                    Long entityId,
                    String action,
                    String oldValue,
                    String newValue,
                    User performedBy,
                    String ipAddress,
                    String description) {

        AuditLog log = new AuditLog();
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setPerformedBy(performedBy);
        log.setIpAddress(ipAddress);
        log.setDescription(description);

        auditLogRepository.save(log);
    }

    /** Shorthand for system-triggered events with no user context */
    public void logSystem(String entityName, Long entityId,
                          String action, String description) {
        log(entityName, entityId, action, null, null, null, null, description);
    }
}