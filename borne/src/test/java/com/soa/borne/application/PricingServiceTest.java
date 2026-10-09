package com.soa.borne.application;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    private final PricingService pricing = new PricingService(
            10, 20, .30, .45, .60, 100, 500, .002, .003, .005);

    @Test
    void calculatesElectricityProgressivelyAcrossTiers() {
        assertPrice(BorneType.ELECTRICITY, 0, 0);
        assertPrice(BorneType.ELECTRICITY, 5, 1.50);
        assertPrice(BorneType.ELECTRICITY, 10, 3.00);
        assertPrice(BorneType.ELECTRICITY, 15, 5.25);
        assertPrice(BorneType.ELECTRICITY, 20, 7.50);
        assertPrice(BorneType.ELECTRICITY, 25, 10.50);
    }

    @Test
    void calculatesWaterUsingLitersTariff() {
        assertPrice(BorneType.WATER, 250, .65);
        assertPrice(BorneType.WATER, 600, 1.90);
    }

    private void assertPrice(BorneType type, double amount, double expected) {
        Borne borne = new Borne("pricing", type, 1);
        borne.consume(amount);
        assertThat(pricing.calculate(borne)).isEqualTo(expected);
    }
}
