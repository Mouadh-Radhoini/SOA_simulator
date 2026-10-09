package com.soa.borne.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorneTest {

    @Test
    void createsElectricityBorneWithZeroConsumption() {
        Borne borne = new Borne("B001", BorneType.ELECTRICITY, 7.2);

        assertThat(borne.getType()).isEqualTo(BorneType.ELECTRICITY);
        assertThat(borne.getConsumption()).isZero();
        assertThat(borne.getPowerKw()).isEqualTo(7.2);
    }

    @Test
    void recordsWaterConsumptionInLiters() {
        Borne borne = new Borne("B002", BorneType.WATER, 12);

        borne.consume(250);

        assertThat(borne.getConsumption()).isEqualTo(250);
        assertThat(borne.getFlowRateLitersPerMinute()).isEqualTo(12);
    }

    @Test
    void followsSupplyPointLifecycle() {
        Borne borne = new Borne("B001", BorneType.ELECTRICITY, 7.2);

        borne.reserve();
        borne.startUse();
        borne.stopUse();

        assertThat(borne.getStatus()).isEqualTo(BorneStatus.AVAILABLE);
    }

    @Test
    void rejectsInvalidTransitionsAndNegativeConsumption() {
        Borne borne = new Borne("B001", BorneType.ELECTRICITY, 7.2);

        assertThatThrownBy(borne::startUse)
                .isInstanceOf(InvalidBorneTransitionException.class);
        assertThatThrownBy(() -> borne.consume(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
