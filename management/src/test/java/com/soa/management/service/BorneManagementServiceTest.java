package com.soa.management.service;

import com.soa.management.docker.DockerContainerManager;
import com.soa.management.registry.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class BorneManagementServiceTest {
    @Mock BorneInstanceRepository repository;
    @Mock DockerContainerManager docker;
    @Test void createsAndStartsOneContainer() {
        when(repository.existsByBorneId("B001")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(docker.containerName("B001")).thenReturn("borne-b001");
        when(docker.createBorneContainer("B001")).thenReturn(
                new DockerContainerManager.BorneContainer("id", "borne-b001", "created", null, java.util.Map.of()));
        var service = new BorneManagementService(repository, docker, "soa-borne:local");
        var result = service.create(1);
        assertEquals("B001", result.getFirst().getBorneId());
        verify(docker).startBorneContainer("id");
    }
}
