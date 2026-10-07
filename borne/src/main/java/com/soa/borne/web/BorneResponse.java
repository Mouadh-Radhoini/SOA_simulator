package com.soa.borne.web;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneStatus;

import java.time.Instant;

public record BorneResponse(
        String id,
        BorneStatus status,
        double energyKwh,
        double powerKw,
        double batteryCapacityKwh,
        Instant chargingStartedAt,
        Instant createdAt,
        Instant updatedAt) {

    public static BorneResponse from(Borne borne) {
        return new BorneResponse(
                borne.getId(),
                borne.getStatus(),
                borne.getEnergyKwh(),
                borne.getPowerKw(),
                borne.getBatteryCapacityKwh(),
                borne.getChargingStartedAt(),
                borne.getCreatedAt(),
                borne.getUpdatedAt());
    }
}
