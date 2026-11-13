package com.soa.borne.mqtt;

import java.util.UUID;

public interface MqttEventPublisher {
    void enqueue(String eventType, String borneId, Object payload, UUID correlationId);
}
