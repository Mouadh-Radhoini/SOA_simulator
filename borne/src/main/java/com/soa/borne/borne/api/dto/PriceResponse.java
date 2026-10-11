package com.soa.borne.borne.api.dto;

import com.soa.borne.borne.domain.Reservation;
import java.math.BigDecimal;

public record PriceResponse(BigDecimal electricityCost, BigDecimal waterCost, BigDecimal totalCost) {
    public static PriceResponse from(Reservation r) {
        return new PriceResponse(r.getElectricityCost(), r.getWaterCost(), r.getTotalCost());
    }
}
