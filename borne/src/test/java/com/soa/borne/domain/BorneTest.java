package com.soa.borne.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorneTest {

    @Test
    void followsReserveChargeAndReleaseLifecycle() {
        Borne borne = new Borne("b-1", 10);
        Instant start = Instant.parse("2026-01-01T00:00:00Z");

        borne.reserve();
        borne.startCharging(start);
        borne.applyChargingEnergy(start.plusSeconds(1800), 10);
        borne.stopCharging();

        assertThat(borne.getStatus()).isEqualTo(BorneStatus.AVAILABLE);
        assertThat(borne.getEnergyKwh()).isEqualTo(5.0);
    }

    @Test
    void rejectsChargingWithoutReservation() {
        Borne borne = new Borne("b-1", 10);

        assertThatThrownBy(() -> borne.startCharging(Instant.now()))
                .isInstanceOf(InvalidBorneTransitionException.class);
    }

    @Test
    void automaticallyBecomesAvailableWhenBatteryIsFull() {
        Borne borne = new Borne("b-1", 1);
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        borne.reserve();
        borne.startCharging(start);
        borne.applyChargingEnergy(start.plusSeconds(3600), 2);

        assertThat(borne.getStatus()).isEqualTo(BorneStatus.AVAILABLE);
        assertThat(borne.getEnergyKwh()).isEqualTo(1.0);
    }
}
