package com.soa.borne.borne;
import com.soa.borne.borne.domain.Borne;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BorneRepositoryTest {
    @Test void domainRecordHasIndependentDefaults() {
        Borne borne = new Borne("B001", 7.2, 12);
        assertThat(borne.getElectricityKwh()).isZero();
        assertThat(borne.getWaterLiters()).isZero();
    }
}
