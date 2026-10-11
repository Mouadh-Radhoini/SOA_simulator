package com.soa.borne.borne;

import com.soa.borne.borne.domain.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class BorneTest {
    @Test void everyBorneTracksBothUtilities() {
        Borne borne = new Borne("B001", 7.2, 12);
        borne.reserve(); borne.startUse();
        borne.addConsumption(new BigDecimal("2.5"), new BigDecimal("100"));
        assertThat(borne.getElectricityKwh()).isEqualByComparingTo("2.5");
        assertThat(borne.getWaterLiters()).isEqualByComparingTo("100");
    }
    @Test void rejectsInvalidLifecycleAndNegativeUsage() {
        Borne borne = new Borne("B001", 7.2, 12);
        assertThatThrownBy(() -> borne.startUse()).isInstanceOf(InvalidBorneTransitionException.class);
        borne.reserve(); borne.startUse();
        assertThatThrownBy(() -> borne.addConsumption(BigDecimal.ONE, BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
