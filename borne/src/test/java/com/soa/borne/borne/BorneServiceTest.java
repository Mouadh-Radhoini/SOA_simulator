package com.soa.borne.borne;
import com.soa.borne.borne.application.BorneService;
import com.soa.borne.borne.domain.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class BorneServiceTest {
    @Test void retrievesExistingBorne() {
        BorneRepository repository = Mockito.mock(BorneRepository.class);
        Borne borne = new Borne("B001", 7.2, 12);
        when(repository.findById("B001")).thenReturn(Optional.of(borne));
        assertThat(new BorneService(repository).findById("B001")).isSameAs(borne);
    }
}
