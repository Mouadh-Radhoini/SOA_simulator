package com.soa.borne.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "borne.mqtt.enabled", havingValue = "true", matchIfMissing = true)
public class MqttPublisher implements MqttEventPublisher {
    private final MqttOutboxRepository outbox;
    private final ObjectMapper mapper;
    private final String brokerUrl, username, password, clientId, portId, borneId;
    private final int qos;
    private final long reconnectDelay;
    private MqttClient client;

    public MqttPublisher(MqttOutboxRepository outbox,
                          @Value("${borne.mqtt.broker-url}") String brokerUrl,
                          @Value("${borne.mqtt.username}") String username,
                          @Value("${borne.mqtt.password}") String password,
                          @Value("${borne.mqtt.client-id}") String clientId,
                          @Value("${borne.mqtt.port-id}") String portId,
                          @Value("${borne.id}") String borneId,
                          @Value("${borne.mqtt.qos:1}") int qos,
                          @Value("${borne.mqtt.reconnect-delay-ms:5000}") long reconnectDelay) {
        this.outbox = outbox; this.mapper = JsonMapper.builder().addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();
        this.brokerUrl = brokerUrl; this.username = username;
        this.password = password; this.clientId = clientId; this.portId = portId; this.borneId = borneId;
        this.qos = qos; this.reconnectDelay = reconnectDelay;
    }

    @PostConstruct
    void connect() { tryConnect(); }

    public void enqueue(String eventType, String targetBorneId, Object payload, UUID correlationId) {
        try {
            UUID id = UUID.randomUUID();
            MqttEvent event = new MqttEvent(id, eventType, 1, portId, targetBorneId, Instant.now(), correlationId, payload);
            String topic = "soa/v1/ports/" + portId + "/bornes/" + targetBorneId + "/" + topicPart(eventType);
            outbox.save(new MqttOutbox(id, eventType, topic, mapper.writeValueAsString(event)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create MQTT outbox event", exception);
        }
    }

    @Scheduled(fixedDelayString = "${borne.mqtt.reconnect-delay-ms:5000}")
    void publishPending() {
        if (!isConnected()) tryConnect();
        if (!isConnected()) return;
        for (MqttOutbox event : outbox.findTop50ByPublishedFalseOrderByCreatedAtAsc()) {
            try {
                event.attempted();
                client.publish(event.getTopic(), event.getPayload().getBytes(StandardCharsets.UTF_8), qos, false);
                event.published();
                outbox.save(event);
            } catch (MqttException ignored) {
                outbox.save(event);
                break;
            }
        }
    }

    private void tryConnect() {
        try {
            if (client == null) client = new MqttClient(brokerUrl, clientId);
            if (!client.isConnected()) {
                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(username); options.setPassword(password.toCharArray());
                options.setAutomaticReconnect(true); options.setCleanSession(false);
                options.setKeepAliveInterval(30);
                options.setWill("soa/v1/ports/" + portId + "/bornes/" + borneId + "/availability",
                        availability("OFFLINE"), 1, true);
                client.connect(options);
                client.publish("soa/v1/ports/" + portId + "/bornes/" + borneId + "/availability",
                        availability("ONLINE"), 1, true);
            }
        } catch (Exception ignored) { }
    }

    private byte[] availability(String status) {
        try {
            return mapper.writeValueAsBytes(new MqttEvent(UUID.randomUUID(), "BORNE_AVAILABILITY_CHANGED", 1,
                    portId, borneId, Instant.now(), null, java.util.Map.of("availability", status)));
        } catch (Exception exception) { throw new IllegalStateException(exception); }
    }
    private boolean isConnected() { return client != null && client.isConnected(); }
    private static String topicPart(String eventType) {
        return switch (eventType) { case "CONSUMPTION_RECORDED" -> "consumption"; case "BORNE_STATUS_CHANGED" -> "status";
            case "BORNE_ALERT_RAISED" -> "alerts"; default -> "availability"; };
    }
    @PreDestroy void close() { try { if (client != null && client.isConnected()) client.disconnect(); } catch (Exception ignored) { } }
}
