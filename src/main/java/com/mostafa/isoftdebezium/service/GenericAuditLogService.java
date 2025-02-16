package com.mostafa.isoftdebezium.service;


import com.mostafa.isoftdebezium.entity.GenericAuditLog;
import com.mostafa.isoftdebezium.repository.GenericAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GenericAuditLogService {

    @Autowired
    private GenericAuditLogRepository genericAuditLogRepository;

    @Transactional
    public void saveAuditLog(String tableName, String oldData, String newData, String operationType) {
        GenericAuditLog auditLog = new GenericAuditLog();
        auditLog.setTableName(tableName);
        auditLog.setOldData(oldData);
        auditLog.setNewData(newData);
        auditLog.setOperationType(operationType);
        auditLog.setCreatedAt(LocalDateTime.now());

        genericAuditLogRepository.save(auditLog);
    }
}
