package com.soa.borne.infrastructure.persistence;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BorneRepositoryTest {

    @Autowired
    private BorneRepository repository;

    @Test
    void persistsAndFindsBornesByStatus() {
        repository.saveAndFlush(new Borne("persisted-borne", 40));

        assertThat(repository.findByStatus(BorneStatus.AVAILABLE))
                .extracting(Borne::getId)
                .contains("persisted-borne");
    }
}
