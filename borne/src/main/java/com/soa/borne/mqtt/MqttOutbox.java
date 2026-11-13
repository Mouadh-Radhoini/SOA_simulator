package com.soa.borne.mqtt;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mqtt_outbox")
public class MqttOutbox {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "event_id", nullable = false, unique = true) private UUID eventId;
    @Column(name = "event_type", nullable = false) private String eventType;
    @Column(nullable = false, length = 500) private String topic;
    @Column(nullable = false, columnDefinition = "TEXT") private String payload;
    @Column(nullable = false) private boolean published;
    @Column(nullable = false) private int attempts;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "published_at") private Instant publishedAt;
    protected MqttOutbox() {}
    public MqttOutbox(UUID eventId, String eventType, String topic, String payload) {
        this.eventId = eventId; this.eventType = eventType; this.topic = topic; this.payload = payload;
        this.createdAt = Instant.now();
    }
    public void published() { published = true; publishedAt = Instant.now(); }
    public void attempted() { attempts++; }
    public Long getId() { return id; } public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; } public String getTopic() { return topic; }
    public String getPayload() { return payload; } public boolean isPublished() { return published; }
}
