package com.soa.borne.application;

import java.util.List;

public final class ProgressivePricingStrategy implements PricingStrategy {

    public record Tier(Double upperBound, double pricePerUnit) {
        public Tier {
            if (upperBound != null
                    && (Double.isNaN(upperBound) || Double.isInfinite(upperBound)
                    || upperBound <= 0)) {
                throw new IllegalArgumentException("Tier upper bound must be positive");
            }
            if (Double.isNaN(pricePerUnit) || Double.isInfinite(pricePerUnit)
                    || pricePerUnit < 0) {
                throw new IllegalArgumentException("Price per unit must not be negative");
            }
        }
    }

    private final List<Tier> tiers;

    public ProgressivePricingStrategy(List<Tier> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("At least one pricing tier is required");
        }
        this.tiers = List.copyOf(tiers);
        for (int index = 1; index < this.tiers.size(); index++) {
            Double previous = this.tiers.get(index - 1).upperBound();
            Double current = this.tiers.get(index).upperBound();
            if (previous == null || (current != null && current <= previous)) {
                throw new IllegalArgumentException("Pricing tiers must be ordered and end with one open tier");
            }
        }
    }

    @Override
    public double calculate(double consumption) {
        if (Double.isNaN(consumption) || Double.isInfinite(consumption) || consumption < 0) {
            throw new IllegalArgumentException("Consumption must not be negative");
        }
        double total = 0;
        double previousBound = 0;
        for (Tier tier : tiers) {
            double tierAmount = tier.upperBound() == null
                    ? consumption - previousBound
                    : Math.min(consumption, tier.upperBound()) - previousBound;
            if (tierAmount > 0) {
                total += tierAmount * tier.pricePerUnit();
            }
            if (tier.upperBound() != null) {
                previousBound = tier.upperBound();
            }
            if (consumption <= previousBound) {
                break;
            }
        }
        return total;
    }
}
