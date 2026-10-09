package com.soa.borne.web;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneStatus;

import java.time.Instant;

public record BorneResponse(
        String id,
        com.soa.borne.domain.BorneType type,
        BorneStatus status,
        double consumption,
        String unit,
        Double powerKw,
        Double flowRateLitersPerMinute,
        Instant createdAt,
        Instant updatedAt) {

    public static BorneResponse from(Borne borne) {
        return new BorneResponse(
                borne.getId(),
                borne.getType(),
                borne.getStatus(),
                borne.getConsumption(),
                borne.getType() == com.soa.borne.domain.BorneType.ELECTRICITY ? "kWh" : "L",
                borne.getPowerKw(),
                borne.getFlowRateLitersPerMinute(),
                borne.getCreatedAt(),
                borne.getUpdatedAt());
    }
}
