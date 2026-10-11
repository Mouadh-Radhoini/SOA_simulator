package com.soa.borne.simulator;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorConfigurationTest {
    @Test
    void parsesSharedApiConfiguration() {
        SimulatorConfig config = SimulatorConfig.from(new String[]{
                "--count=3", "--api-url=http://localhost:9090/",
                "--max-workers=2", "--interval-ms=250", "--request-timeout-ms=700"});
        assertEquals(3, config.count());
        assertEquals(URI.create("http://localhost:9090/"), config.apiBaseUrl());
        assertEquals(2, config.maxWorkers());
        assertEquals(Duration.ofMillis(250), config.interval());
        assertEquals("test-001", new SimulatorConfig(1, "test-", URI.create("http://localhost:8080"),
                1, Duration.ofSeconds(1), Duration.ofSeconds(1), 0, 7.2, 12).withCount(2).borneId(0));
    }

    @Test
    void rejectsInvalidCountsAndMalformedNumbers() {
        assertThrows(IllegalArgumentException.class, () -> SimulatorConfig.from(new String[]{"--count=-1"}));
        assertThrows(IllegalArgumentException.class, () -> SimulatorConfig.from(new String[]{"--count=abc"}));
        assertThrows(IllegalArgumentException.class, () -> SimulatorConfig.from(new String[]{"--count=101"}));
    }
}
