package com.soa.borne.application;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorneServiceTest {

    @Mock
    private BorneStore repository;

    @Test
    void recordsConsumptionAndPersistsIt() {
        Borne borne = new Borne("B001", BorneType.ELECTRICITY, 7.2);
        when(repository.findById("B001")).thenReturn(Optional.of(borne));
        when(repository.save(any(Borne.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BorneService service = new BorneService(repository, new PricingService(
                10, 20, .30, .45, .60, 100, 500, .002, .003, .005));

        service.consume("B001", 5.5);

        assertThat(borne.getConsumption()).isEqualTo(5.5);
    }
}
