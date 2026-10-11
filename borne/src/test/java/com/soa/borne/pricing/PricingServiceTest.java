package com.soa.borne.pricing;

import com.soa.borne.pricing.domain.ProgressivePricingStrategy;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {
    private final ProgressivePricingStrategy electricity = new ProgressivePricingStrategy(List.of(
            new ProgressivePricingStrategy.Tier(BigDecimal.TEN, new BigDecimal("0.30")),
            new ProgressivePricingStrategy.Tier(new BigDecimal("20"), new BigDecimal("0.45")),
            new ProgressivePricingStrategy.Tier(null, new BigDecimal("0.60"))));
    private final ProgressivePricingStrategy water = new ProgressivePricingStrategy(List.of(
            new ProgressivePricingStrategy.Tier(new BigDecimal("100"), new BigDecimal("0.002")),
            new ProgressivePricingStrategy.Tier(new BigDecimal("500"), new BigDecimal("0.003")),
            new ProgressivePricingStrategy.Tier(null, new BigDecimal("0.005"))));
    @Test void pricesElectricityBoundaries() {
        assertThat(electricity.calculate(new BigDecimal("10"))).isEqualByComparingTo("3.00");
        assertThat(electricity.calculate(new BigDecimal("14"))).isEqualByComparingTo("4.80");
        assertThat(electricity.calculate(new BigDecimal("20"))).isEqualByComparingTo("7.50");
        assertThat(electricity.calculate(new BigDecimal("25"))).isEqualByComparingTo("10.50");
    }
    @Test void pricesWaterBoundaries() {
        assertThat(water.calculate(new BigDecimal("100"))).isEqualByComparingTo("0.200");
        assertThat(water.calculate(new BigDecimal("500"))).isEqualByComparingTo("1.400");
        assertThat(water.calculate(new BigDecimal("600"))).isEqualByComparingTo("1.900");
    }
}
