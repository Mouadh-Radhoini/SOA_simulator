package com.soa.management.websocket;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record WebSocketMessageDTO(UUID eventId, String eventType, String borneId,
                                  Instant occurredAt, Map<String, Object> data) {}
