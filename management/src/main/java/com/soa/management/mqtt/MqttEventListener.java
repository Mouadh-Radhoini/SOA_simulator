package com.soa.management.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soa.management.registry.BorneInstanceRepository;
import com.soa.management.websocket.WebSocketNotificationService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttEventListener {
    private static final Logger log = LoggerFactory.getLogger(MqttEventListener.class);
    private final ObjectMapper mapper;
    private final ProcessedMqttEventRepository processed;
    private final BorneInstanceRepository bornes;
    private final WebSocketNotificationService notifications;
    private final String brokerUrl, username, password, clientId;
    private final int qos;
    private MqttClient client;

    public MqttEventListener(ObjectMapper mapper, ProcessedMqttEventRepository processed,
                             BorneInstanceRepository bornes, WebSocketNotificationService notifications,
                             @Value("${mqtt.broker-url}") String brokerUrl, @Value("${mqtt.username}") String username,
                             @Value("${mqtt.password}") String password, @Value("${mqtt.client-id}") String clientId,
                             @Value("${mqtt.qos:1}") int qos) {
        this.mapper = mapper; this.processed = processed; this.bornes = bornes; this.notifications = notifications;
        this.brokerUrl = brokerUrl; this.username = username; this.password = password; this.clientId = clientId; this.qos = qos;
    }

    @PostConstruct
    void start() {
        try {
            client = new MqttClient(brokerUrl, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username); options.setPassword(password.toCharArray());
            options.setAutomaticReconnect(true); options.setCleanSession(false); options.setKeepAliveInterval(30);
            client.connect(options);
            client.subscribe("soa/v1/ports/+/bornes/+/#", qos, (topic, message) -> process(message.getPayload()));
        } catch (Exception ignored) { }
    }

    @Transactional
    void process(byte[] bytes) {
        try {
            JsonNode event = mapper.readTree(bytes);
            UUID eventId = UUID.fromString(required(event, "eventId"));
            String type = required(event, "eventType");
            String borneId = required(event, "borneId");
            if (processed.existsById(eventId)) return;
            JsonNode payload = event.path("payload");
            if ("BORNE_AVAILABILITY_CHANGED".equals(type)) {
                bornes.findByBorneId(borneId).ifPresent(b -> {
                    b.updateMqtt(payload.path("availability").asText("UNKNOWN"));
                    bornes.save(b);
                });
            }
            processed.save(new ProcessedMqttEvent(eventId, type, borneId));
            notifications.publish(eventId, type, borneId,
                    Instant.parse(required(event, "occurredAt")),
                    toMap(payload));
        } catch (Exception exception) {
            log.warn("Rejected MQTT event payload", exception);
        }
    }
    private static String required(JsonNode node, String field) {
        if (!node.hasNonNull(field) || node.get(field).asText().isBlank()) throw new IllegalArgumentException("Missing " + field);
        return node.get(field).asText();
    }
    private static Map<String, Object> toMap(JsonNode node) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> result.put(entry.getKey(), toJavaValue(entry.getValue())));
        }
        return result;
    }
    private static Object toJavaValue(JsonNode node) {
        if (node.isObject()) {
            Map<String, Object> result = new LinkedHashMap<>();
            node.fields().forEachRemaining(entry -> result.put(entry.getKey(), toJavaValue(entry.getValue())));
            return result;
        }
        if (node.isArray()) {
            List<Object> result = new ArrayList<>();
            node.elements().forEachRemaining(value -> result.add(toJavaValue(value)));
            return result;
        }
        if (node.isNull()) return null;
        if (node.isBoolean()) return node.booleanValue();
        if (node.isIntegralNumber()) return node.longValue();
        if (node.isFloatingPointNumber()) return node.doubleValue();
        return node.asText();
    }
    @PreDestroy void stop() { try { if (client != null && client.isConnected()) client.disconnect(); } catch (Exception ignored) { } }
}
