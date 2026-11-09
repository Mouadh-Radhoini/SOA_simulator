package com.soa.management.registry;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "borne_instance")
public class BorneInstance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "borne_id", nullable = false, unique = true) private String borneId;
    @Column(name = "container_id") private String containerId;
    @Column(name = "container_name", nullable = false, unique = true) private String containerName;
    @Column(name = "image_name", nullable = false) private String imageName;
    @Column(name = "internal_port", nullable = false) private int internalPort = 8080;
    @Enumerated(EnumType.STRING) @Column(name = "container_status", nullable = false) private ContainerStatus containerStatus;
    @Enumerated(EnumType.STRING) @Column(name = "application_health_status", nullable = false) private TechnicalHealthStatus applicationHealthStatus;
    @Column(name = "last_health_check") private Instant lastHealthCheck;
    @Column(name = "consecutive_failures", nullable = false) private int consecutiveFailures;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Column(name = "last_error", length = 1000) private String lastError;
    protected BorneInstance() {}
    public BorneInstance(String borneId, String containerName, String imageName) {
        this.borneId = borneId; this.containerName = containerName; this.imageName = imageName;
        this.containerStatus = ContainerStatus.CREATED; this.applicationHealthStatus = TechnicalHealthStatus.STARTING;
        this.createdAt = Instant.now(); this.updatedAt = this.createdAt;
    }
    public void update(String containerId, ContainerStatus containerStatus, TechnicalHealthStatus health,
                       int failures, String error) {
        this.containerId = containerId; this.containerStatus = containerStatus; this.applicationHealthStatus = health;
        this.consecutiveFailures = failures; this.lastError = error; this.lastHealthCheck = Instant.now(); this.updatedAt = Instant.now();
    }
    public Long getId() { return id; } public String getBorneId() { return borneId; }
    public String getContainerId() { return containerId; } public String getContainerName() { return containerName; }
    public String getImageName() { return imageName; } public int getInternalPort() { return internalPort; }
    public ContainerStatus getContainerStatus() { return containerStatus; }
    public TechnicalHealthStatus getApplicationHealthStatus() { return applicationHealthStatus; }
    public Instant getLastHealthCheck() { return lastHealthCheck; } public int getConsecutiveFailures() { return consecutiveFailures; }
    public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
    public String getLastError() { return lastError; }
}
