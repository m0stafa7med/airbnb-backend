package com.mostafa.isoftdebezium.repository;

import com.mostafa.isoftdebezium.entity.GenericAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenericAuditLogRepository extends JpaRepository<GenericAuditLog, Long> {
}
