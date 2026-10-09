package com.soa.borne.simulator;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatorConfigurationTest {

    @Test
    void parsesCommandLineConfiguration() {
        SimulatorConfiguration configuration = SimulatorConfiguration.from(new String[]{
                "--count=2", "--base-port=9000", "--borne-id-prefix=test-", "--borne-jar=app.jar"});

        assertEquals(2, configuration.count());
        assertEquals(9000, configuration.basePort());
        assertEquals("test-", configuration.borneIdPrefix());
        assertEquals(Path.of("app.jar"), configuration.borneJar());
    }

    @Test
    void buildsDistinctCommandsForEachBorne() throws Exception {
        SimulatorConfiguration configuration = new SimulatorConfiguration(
                2, 9000, "test-", Path.of("borne.jar"), "java");
        List<List<String>> commands = new ArrayList<>();
        BorneProcessManager manager = new BorneProcessManager(configuration, command -> {
            commands.add(command);
            return new ProcessBuilder("sh", "-c", "exit 0").start();
        });

        assertEquals(2, manager.launchConfiguredBornes().size());
        assertTrue(commands.get(0).contains("--server.port=9000"));
        assertTrue(commands.get(0).contains("--borne.id=test-001"));
        assertTrue(commands.get(1).contains("--server.port=9001"));
        assertTrue(commands.get(1).contains("--borne.id=test-002"));
        assertTrue(commands.get(0).contains("--borne.type=ELECTRICITY"));
        manager.shutdownAll();
    }
}
