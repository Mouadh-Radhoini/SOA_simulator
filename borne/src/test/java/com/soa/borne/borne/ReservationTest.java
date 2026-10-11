package com.soa.borne.borne;

import com.soa.borne.borne.domain.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class ReservationTest {
    @Test void completesAndKeepsIndependentUsageAndCosts() {
        Ship ship = new Ship("S001", "Atlantic Star");
        Borne borne = new Borne("B001", 7.2, 12);
        borne.reserve();
        Reservation reservation = new Reservation(ship, borne);
        reservation.start();
        reservation.consume(new BigDecimal("14"), new BigDecimal("600"));
        reservation.complete(new BigDecimal("4.80"), new BigDecimal("1.90"));
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
        assertThat(reservation.getElectricityKwh()).isEqualByComparingTo("14");
        assertThat(reservation.getWaterLiters()).isEqualByComparingTo("600");
        assertThat(reservation.getTotalCost()).isEqualByComparingTo("6.70");
        assertThat(borne.getStatus()).isEqualTo(BorneStatus.AVAILABLE);
    }

    @Test void cancelledReservationReleasesBorneAndCannotRestart() {
        Borne borne = new Borne("B001", 7.2, 12);
        borne.reserve();
        Reservation reservation = new Reservation(new Ship("S001", "Ship"), borne);
        reservation.cancel();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservation.getBorne().getStatus()).isEqualTo(BorneStatus.AVAILABLE);
        assertThatThrownBy(reservation::start).isInstanceOf(IllegalStateException.class);
    }
}
