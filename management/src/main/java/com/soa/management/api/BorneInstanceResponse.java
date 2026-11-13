package com.soa.management.api;

import com.soa.management.registry.BorneInstance;
import com.soa.management.registry.ContainerStatus;
import com.soa.management.registry.TechnicalHealthStatus;
import java.time.Instant;

public record BorneInstanceResponse(
        Long id, String borneId, String containerId, String containerName, String imageName,
        int internalPort, ContainerStatus containerStatus, TechnicalHealthStatus applicationHealthStatus,
        Instant lastHealthCheck, int consecutiveFailures, Instant createdAt, Instant updatedAt, String lastError,
        String mqttAvailability, Instant lastMqttEventAt) {
    public static BorneInstanceResponse from(BorneInstance instance) {
        return new BorneInstanceResponse(instance.getId(), instance.getBorneId(), instance.getContainerId(),
                instance.getContainerName(), instance.getImageName(), instance.getInternalPort(),
                instance.getContainerStatus(), instance.getApplicationHealthStatus(), instance.getLastHealthCheck(),
                instance.getConsecutiveFailures(), instance.getCreatedAt(), instance.getUpdatedAt(), instance.getLastError(),
                instance.getMqttAvailability(), instance.getLastMqttEventAt());
    }
}
