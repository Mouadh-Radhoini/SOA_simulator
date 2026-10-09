package com.soa.borne.application;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PricingService {

    private final PricingStrategy electricityPricing;
    private final PricingStrategy waterPricing;

    public PricingService(
            @Value("${pricing.electricity.tier-one-limit:10}") double electricityTierOneLimit,
            @Value("${pricing.electricity.tier-two-limit:20}") double electricityTierTwoLimit,
            @Value("${pricing.electricity.tier-one-rate:0.30}") double electricityTierOneRate,
            @Value("${pricing.electricity.tier-two-rate:0.45}") double electricityTierTwoRate,
            @Value("${pricing.electricity.tier-three-rate:0.60}") double electricityTierThreeRate,
            @Value("${pricing.water.tier-one-limit:100}") double waterTierOneLimit,
            @Value("${pricing.water.tier-two-limit:500}") double waterTierTwoLimit,
            @Value("${pricing.water.tier-one-rate:0.002}") double waterTierOneRate,
            @Value("${pricing.water.tier-two-rate:0.003}") double waterTierTwoRate,
            @Value("${pricing.water.tier-three-rate:0.005}") double waterTierThreeRate) {
        electricityPricing = new ProgressivePricingStrategy(List.of(
                new ProgressivePricingStrategy.Tier(electricityTierOneLimit, electricityTierOneRate),
                new ProgressivePricingStrategy.Tier(electricityTierTwoLimit, electricityTierTwoRate),
                new ProgressivePricingStrategy.Tier(null, electricityTierThreeRate)));
        waterPricing = new ProgressivePricingStrategy(List.of(
                new ProgressivePricingStrategy.Tier(waterTierOneLimit, waterTierOneRate),
                new ProgressivePricingStrategy.Tier(waterTierTwoLimit, waterTierTwoRate),
                new ProgressivePricingStrategy.Tier(null, waterTierThreeRate)));
    }

    public double calculate(Borne borne) {
        return strategyFor(borne.getType()).calculate(borne.getConsumption());
    }

    public String unitFor(BorneType type) {
        return type == BorneType.ELECTRICITY ? "kWh" : "L";
    }

    private PricingStrategy strategyFor(BorneType type) {
        return type == BorneType.ELECTRICITY ? electricityPricing : waterPricing;
    }
}
