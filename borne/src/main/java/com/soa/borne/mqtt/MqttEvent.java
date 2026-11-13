package com.soa.borne.mqtt;

import java.time.Instant;
import java.util.UUID;

public record MqttEvent(UUID eventId, String eventType, int schemaVersion, String portId,
                        String borneId, Instant occurredAt, UUID correlationId, Object payload) {
}
