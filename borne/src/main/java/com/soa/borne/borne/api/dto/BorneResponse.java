package com.soa.borne.borne.api.dto;

import com.soa.borne.borne.domain.Borne;
import com.soa.borne.borne.domain.BorneStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record BorneResponse(String id, BorneStatus status, BigDecimal electricityKwh,
                            BigDecimal waterLiters, double powerKw, double waterFlowLitersPerMinute,
                            Instant createdAt, Instant updatedAt) {
    public static BorneResponse from(Borne b) {
        return new BorneResponse(b.getId(), b.getStatus(), b.getElectricityKwh(), b.getWaterLiters(),
                b.getPowerKw(), b.getFlowRateLitersPerMinute(), b.getCreatedAt(), b.getUpdatedAt());
    }
}
