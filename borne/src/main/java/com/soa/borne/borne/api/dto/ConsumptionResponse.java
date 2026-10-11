package com.soa.borne.borne.api.dto;

import com.soa.borne.borne.domain.Reservation;
import java.math.BigDecimal;

public record ConsumptionResponse(String reservationId, BigDecimal electricityKwh,
                                  BigDecimal waterLiters, BigDecimal electricityCost,
                                  BigDecimal waterCost, BigDecimal totalCost) {
    public static ConsumptionResponse from(Reservation r) {
        return new ConsumptionResponse(r.getId().toString(), r.getElectricityKwh(), r.getWaterLiters(),
                r.getElectricityCost(), r.getWaterCost(), r.getTotalCost());
    }
}
