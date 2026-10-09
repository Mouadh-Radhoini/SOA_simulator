package com.soa.borne.infrastructure.persistence;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneStatus;
import com.soa.borne.domain.BorneType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BorneRepositoryTest {

    @Autowired
    private BorneRepository repository;

    @Test
    void persistsSupplyPointByStatus() {
        repository.saveAndFlush(new Borne("B001", BorneType.WATER, 12));

        assertThat(repository.findByStatus(BorneStatus.AVAILABLE))
                .extracting(Borne::getId)
                .contains("B001");
    }
}
