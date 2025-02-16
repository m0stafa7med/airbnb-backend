package com.mostafa.isoftdebezium.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mostafa.isoftdebezium.service.GenericAuditLogService;
import io.debezium.config.Configuration;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static io.debezium.data.Envelope.FieldName.*;
import static io.debezium.data.Envelope.Operation;

@Slf4j
@Component
public class DebeziumListener {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;
    private final GenericAuditLogService genericAuditLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public DebeziumListener(Configuration customerConnectorConfiguration, GenericAuditLogService genericAuditLogService) {
        this.genericAuditLogService = genericAuditLogService;
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(customerConnectorConfiguration.asProperties())
                .notifying(this::handleChangeEvent)
                .build();
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        logRecordDetails(sourceRecord);

        Struct sourceRecordChangeValue = (Struct) sourceRecord.value();
        if (sourceRecordChangeValue == null)
            return;


        Operation operation = extractOperation(sourceRecordChangeValue);
        if (operation == Operation.READ)
            return;


        String tableName = extractTableName(sourceRecord);
        Map<String, Object> payload = extractPayload(sourceRecordChangeValue, operation);
        String oldData = extractOldData(sourceRecordChangeValue, operation);
        String newData = extractNewData(payload, operation);

        saveAuditLog(tableName, oldData, newData, operation);
        logUpdatedData(payload, operation);
    }

    private void logRecordDetails(SourceRecord sourceRecord) {
        log.info("Key = {}, Value = {}", sourceRecord.key(), sourceRecord.value());
    }

    private Operation extractOperation(Struct sourceRecordChangeValue) {
        String operationCode = sourceRecordChangeValue.get(OPERATION).toString();
        return Operation.forCode(operationCode);
    }

    private String extractTableName(SourceRecord sourceRecord) {
        return sourceRecord.topic();
    }

    private Map<String, Object> extractPayload(Struct sourceRecordChangeValue, Operation operation) {
        String recordType = operation == Operation.DELETE ? BEFORE : AFTER;
        Struct struct = (Struct) sourceRecordChangeValue.get(recordType);
        return struct.schema().fields().stream()
                .map(Field::name)
                .filter(fieldName -> struct.get(fieldName) != null)
                .collect(Collectors.toMap(fieldName -> fieldName, struct::get));
    }

    private String extractOldData(Struct sourceRecordChangeValue, Operation operation) {
        if (operation != Operation.UPDATE && operation != Operation.DELETE)
            return null;


        Struct oldStruct = (Struct) sourceRecordChangeValue.get(BEFORE);
        if (oldStruct == null)
            return null;


        Map<String, Object> oldPayload = oldStruct.schema().fields().stream()
                .map(Field::name)
                .filter(fieldName -> oldStruct.get(fieldName) != null)
                .collect(Collectors.toMap(fieldName -> fieldName, oldStruct::get));

        return serializeToJson(oldPayload);
    }

    private String extractNewData(Map<String, Object> payload, Operation operation) {
        if (operation != Operation.CREATE && operation != Operation.UPDATE)
            return null;

        return serializeToJson(payload);
    }

    private String serializeToJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload to JSON", e);
            return null;
        }
    }

    private void saveAuditLog(String tableName, String oldData, String newData, Operation operation) {
        genericAuditLogService.saveAuditLog(tableName, oldData, newData, operation.name());
    }

    private void logUpdatedData(Map<String, Object> payload, Operation operation) {
        log.info("Updated Data: {} with Operation: {}", payload, operation.name());
    }

    @PostConstruct
    private void start() {
        this.executor.execute(debeziumEngine);
    }

    @PreDestroy
    private void stop() throws IOException {
        if (Objects.nonNull(this.debeziumEngine)) {
            this.debeziumEngine.close();
        }
    }
}
