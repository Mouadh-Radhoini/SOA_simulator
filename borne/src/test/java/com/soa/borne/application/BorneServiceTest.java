package com.soa.borne.application;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorneServiceTest {

    @Mock
    private BorneStore repository;

    @Test
    void schedulerUpdatesEnergyOnChargingBornes() {
        Instant now = Instant.parse("2026-01-01T01:00:00Z");
        Borne borne = new Borne("service-borne", 20);
        borne.reserve();
        borne.startCharging(now.minusSeconds(1800));
        when(repository.findByStatus(BorneStatus.CHARGING)).thenReturn(List.of(borne));
        BorneService service = new BorneService(
                repository, Clock.fixed(now, ZoneOffset.UTC), 10);

        service.updateChargingEnergy();

        assertThat(borne.getEnergyKwh()).isEqualTo(5);
        verify(repository).save(borne);
    }

    @Test
    void delegatesReserveTransitionAndPersistsIt() {
        Borne borne = new Borne("service-borne", 20);
        when(repository.findById("service-borne")).thenReturn(Optional.of(borne));
        when(repository.save(any(Borne.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BorneService service = new BorneService(repository, Clock.systemUTC(), 7.2);

        service.reserve("service-borne");

        assertThat(borne.getStatus()).isEqualTo(BorneStatus.RESERVED);
        verify(repository).save(borne);
    }
}
