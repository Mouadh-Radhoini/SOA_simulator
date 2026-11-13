package com.soa.management.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soa.management.registry.BorneInstanceRepository;
import com.soa.management.websocket.WebSocketNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MqttEventListenerTest {
    private ProcessedMqttEventRepository processed;
    private BorneInstanceRepository bornes;
    private WebSocketNotificationService notifications;
    private MqttEventListener listener;

    @BeforeEach
    void setUp() {
        processed = mock(ProcessedMqttEventRepository.class);
        bornes = mock(BorneInstanceRepository.class);
        notifications = mock(WebSocketNotificationService.class);
        listener = new MqttEventListener(new ObjectMapper(), processed, bornes, notifications,
                "tcp://localhost:1883", "management", "secret", "test-client", 1);
    }

    @Test
    void duplicateEventsAreIgnored() {
        UUID id = UUID.randomUUID();
        when(processed.existsById(id)).thenReturn(true);
        listener.process(event(id, "CONSUMPTION_RECORDED"));
        verify(processed, never()).save(any());
        verifyNoInteractions(notifications);
    }

    @Test
    void malformedEventsDoNotChangeState() {
        listener.process("{\"eventType\":\"CONSUMPTION_RECORDED\"}".getBytes(StandardCharsets.UTF_8));
        verifyNoInteractions(processed, bornes, notifications);
    }

    @Test
    void validEventsArePersistedAndBroadcast() {
        UUID id = UUID.randomUUID();
        when(processed.existsById(id)).thenReturn(false);
        listener.process(event(id, "CONSUMPTION_RECORDED"));
        verify(processed).save(any(ProcessedMqttEvent.class));
        verify(notifications).publish(eq(id), eq("CONSUMPTION_RECORDED"), eq("B001"), any(), any());
    }

    private byte[] event(UUID id, String type) {
        return ("{\"eventId\":\"" + id + "\",\"eventType\":\"" + type
                + "\",\"schemaVersion\":1,\"portId\":\"PORT001\",\"borneId\":\"B001\","
                + "\"occurredAt\":\"2026-11-11T19:45:00Z\",\"payload\":{\"value\":1}}")
                .getBytes(StandardCharsets.UTF_8);
    }
}
