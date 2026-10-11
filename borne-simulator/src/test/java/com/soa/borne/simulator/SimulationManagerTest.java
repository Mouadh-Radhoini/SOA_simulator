package com.soa.borne.simulator;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SimulationManagerTest {
    @Test
    void createsIndependentTasksAndStopsThemTogether() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/bornes", exchange -> {
            String path = exchange.getRequestURI().getPath();
            int status = path.matches("/api/bornes/B\\d{3}") ? 200 : 200;
            byte[] body = path.matches("/api/bornes/B\\d{3}")
                    ? "{\"id\":\"B001\",\"status\":\"AVAILABLE\"}".getBytes()
                    : "[]".getBytes();
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        URI api = URI.create("http://localhost:" + server.getAddress().getPort());
        SimulatorConfig config = new SimulatorConfig(3, "B", api, 2,
                Duration.ofMillis(20), Duration.ofSeconds(1), 0, 7.2, 12);
        try (SimulationManager manager = new SimulationManager(config,
                new BorneApiClient(api, Duration.ofSeconds(1), 0))) {
            assertEquals(3, manager.start().size());
            assertEquals(3, manager.status().size());
            assertThrows(IllegalStateException.class, manager::start);
            Thread.sleep(100);
            assertTrue(manager.status().values().stream().allMatch(state ->
                    state == BorneSimulationTask.State.CREATED
                            || state == BorneSimulationTask.State.RUNNING
                            || state == BorneSimulationTask.State.STOPPED));
            manager.stop();
            assertTimeoutPreemptively(Duration.ofSeconds(2),
                    () -> assertTrue(manager.status().values().stream().allMatch(
                            state -> state == BorneSimulationTask.State.STOPPED)));
        } finally {
            server.stop(0);
        }
    }
}
