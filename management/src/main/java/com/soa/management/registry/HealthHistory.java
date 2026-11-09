package com.soa.management.registry;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "borne_health_history")
public class HealthHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "borne_id", nullable = false) private String borneId;
    @Column(name = "container_id") private String containerId;
    @Enumerated(EnumType.STRING) @Column(name = "health_status", nullable = false) private TechnicalHealthStatus healthStatus;
    @Column(name = "checked_at", nullable = false) private Instant checkedAt = Instant.now();
    @Column(name = "response_time_ms") private Long responseTimeMs;
    @Column(name = "error_message", length = 1000) private String errorMessage;
    protected HealthHistory() {}
    public HealthHistory(String borneId, String containerId, TechnicalHealthStatus status, Long responseTimeMs, String error) {
        this.borneId = borneId; this.containerId = containerId; this.healthStatus = status;
        this.responseTimeMs = responseTimeMs; this.errorMessage = error;
    }
}
