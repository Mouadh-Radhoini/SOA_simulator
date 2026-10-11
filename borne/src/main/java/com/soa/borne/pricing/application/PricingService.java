package com.soa.borne.pricing.application;

import com.soa.borne.pricing.domain.ProgressivePricingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingService {
    private final ProgressivePricingStrategy electricity;
    private final ProgressivePricingStrategy water;

    public PricingService(
            @Value("${pricing.electricity.tier-one-limit:10}") BigDecimal electricityFirstLimit,
            @Value("${pricing.electricity.tier-two-limit:20}") BigDecimal electricitySecondLimit,
            @Value("${pricing.electricity.tier-one-rate:0.30}") BigDecimal electricityFirstRate,
            @Value("${pricing.electricity.tier-two-rate:0.45}") BigDecimal electricitySecondRate,
            @Value("${pricing.electricity.tier-three-rate:0.60}") BigDecimal electricityThirdRate,
            @Value("${pricing.water.tier-one-limit:100}") BigDecimal waterFirstLimit,
            @Value("${pricing.water.tier-two-limit:500}") BigDecimal waterSecondLimit,
            @Value("${pricing.water.tier-one-rate:0.002}") BigDecimal waterFirstRate,
            @Value("${pricing.water.tier-two-rate:0.003}") BigDecimal waterSecondRate,
            @Value("${pricing.water.tier-three-rate:0.005}") BigDecimal waterThirdRate) {
        electricity = new ProgressivePricingStrategy(List.of(
                new ProgressivePricingStrategy.Tier(electricityFirstLimit, electricityFirstRate),
                new ProgressivePricingStrategy.Tier(electricitySecondLimit, electricitySecondRate),
                new ProgressivePricingStrategy.Tier(null, electricityThirdRate)));
        water = new ProgressivePricingStrategy(List.of(
                new ProgressivePricingStrategy.Tier(waterFirstLimit, waterFirstRate),
                new ProgressivePricingStrategy.Tier(waterSecondLimit, waterSecondRate),
                new ProgressivePricingStrategy.Tier(null, waterThirdRate)));
    }
    public BigDecimal electricityCost(BigDecimal quantity) { return electricity.calculate(quantity); }
    public BigDecimal waterCost(BigDecimal quantity) { return water.calculate(quantity); }
}
