package com.mostafa.isoftdebezium.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "generic_audit_log")
@Getter
@Setter
public class GenericAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "old_data", columnDefinition = "json")
    private String oldData;

    @Column(name = "new_data", columnDefinition = "json")
    private String newData;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
