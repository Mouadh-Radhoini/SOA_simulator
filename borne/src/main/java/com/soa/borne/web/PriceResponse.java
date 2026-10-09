package com.soa.borne.web;

import com.soa.borne.application.PricingService;
import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneType;

public record PriceResponse(
        String borneId,
        BorneType type,
        double consumption,
        String unit,
        double price,
        String currency) {

    public static PriceResponse from(Borne borne, PricingService pricingService) {
        return new PriceResponse(
                borne.getId(),
                borne.getType(),
                borne.getConsumption(),
                pricingService.unitFor(borne.getType()),
                pricingService.calculate(borne),
                "DT");
    }
}
