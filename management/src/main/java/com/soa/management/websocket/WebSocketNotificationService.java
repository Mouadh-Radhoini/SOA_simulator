package com.soa.management.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class WebSocketNotificationService {
    private final SimpMessagingTemplate messaging;
    public WebSocketNotificationService(SimpMessagingTemplate messaging) { this.messaging = messaging; }
    public void publish(UUID eventId, String eventType, String borneId, Instant occurredAt, Map<String, Object> data) {
        WebSocketMessageDTO message = new WebSocketMessageDTO(eventId, eventType, borneId, occurredAt, data);
        String destination = switch (eventType) {
            case "CONSUMPTION_RECORDED" -> "/topic/bornes/telemetry";
            case "BORNE_ALERT_RAISED" -> "/topic/alerts";
            case "BORNE_AVAILABILITY_CHANGED", "BORNE_STATUS_CHANGED" -> "/topic/bornes/status";
            default -> "/topic/bornes/status";
        };
        messaging.convertAndSend(destination, message);
        if ("CONSUMPTION_RECORDED".equals(eventType)) {
            messaging.convertAndSend("/topic/bornes/" + borneId + "/telemetry", message);
        }
        if (eventType.startsWith("BORNE_")) {
            messaging.convertAndSend("/topic/bornes/" + borneId + "/health", message);
        }
    }
}
