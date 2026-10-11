package com.soa.borne.pricing.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class ProgressivePricingStrategy implements PricingStrategy {
    public record Tier(BigDecimal upperBound, BigDecimal pricePerUnit) {
        public Tier {
            if (upperBound != null && upperBound.signum() <= 0) throw new IllegalArgumentException("Tier bound must be positive");
            if (pricePerUnit == null || pricePerUnit.signum() < 0) throw new IllegalArgumentException("Price must not be negative");
        }
    }
    private final List<Tier> tiers;
    public ProgressivePricingStrategy(List<Tier> tiers) {
        if (tiers == null || tiers.isEmpty()) throw new IllegalArgumentException("At least one pricing tier is required");
        this.tiers = List.copyOf(tiers);
        for (int i = 1; i < tiers.size(); i++) {
            BigDecimal previous = tiers.get(i - 1).upperBound();
            BigDecimal current = tiers.get(i).upperBound();
            if (previous == null || (current != null && current.compareTo(previous) <= 0)) {
                throw new IllegalArgumentException("Pricing tiers must be ordered and end with one open tier");
            }
        }
        if (tiers.get(tiers.size() - 1).upperBound() != null) {
            throw new IllegalArgumentException("The last pricing tier must be open-ended");
        }
    }
    public BigDecimal calculate(BigDecimal quantity) {
        if (quantity == null || quantity.signum() < 0) throw new IllegalArgumentException("Quantity must not be negative");
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal previous = BigDecimal.ZERO;
        for (Tier tier : tiers) {
            BigDecimal end = tier.upperBound() == null ? quantity : quantity.min(tier.upperBound());
            BigDecimal amount = end.subtract(previous);
            if (amount.signum() > 0) total = total.add(amount.multiply(tier.pricePerUnit()));
            if (tier.upperBound() == null || quantity.compareTo(tier.upperBound()) <= 0) break;
            previous = tier.upperBound();
        }
        return total.setScale(6, RoundingMode.HALF_UP);
    }
}
