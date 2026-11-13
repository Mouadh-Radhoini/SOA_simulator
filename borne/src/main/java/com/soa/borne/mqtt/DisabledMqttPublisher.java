package com.soa.borne.mqtt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "borne.mqtt.enabled", havingValue = "false")
public class DisabledMqttPublisher implements MqttEventPublisher {
    @Override public void enqueue(String eventType, String borneId, Object payload, UUID correlationId) {
    }
}
