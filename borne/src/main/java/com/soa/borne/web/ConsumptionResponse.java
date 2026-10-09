package com.soa.borne.web;

public record ConsumptionResponse(
        String borneId,
        com.soa.borne.domain.BorneType type,
        double consumption,
        String unit) {
}
