package com.soa.management.mqtt;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_mqtt_event")
public class ProcessedMqttEvent {
    @Id @Column(name = "event_id") private UUID eventId;
    @Column(name = "event_type", nullable = false) private String eventType;
    @Column(name = "borne_id", nullable = false) private String borneId;
    @Column(name = "processed_at", nullable = false) private Instant processedAt;
    @Column(name = "processing_status", nullable = false) private String processingStatus;
    protected ProcessedMqttEvent() {}
    public ProcessedMqttEvent(UUID eventId, String eventType, String borneId) {
        this.eventId = eventId; this.eventType = eventType; this.borneId = borneId;
        this.processedAt = Instant.now(); this.processingStatus = "PROCESSED";
    }
}
