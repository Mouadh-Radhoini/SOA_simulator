package com.soa.management.monitor;

import com.soa.management.docker.DockerContainerManager;
import com.soa.management.registry.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

@Component
public class BorneHealthMonitor {
    private final BorneInstanceRepository instances;
    private final HealthHistoryRepository history;
    private final DockerContainerManager docker;
    private final int failureThreshold;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    public BorneHealthMonitor(BorneInstanceRepository instances, HealthHistoryRepository history, DockerContainerManager docker,
                              @Value("${borne.failure-threshold:3}") int failureThreshold) {
        this.instances = instances; this.history = history; this.docker = docker; this.failureThreshold = failureThreshold;
    }
    @Scheduled(fixedDelayString = "${borne.monitor-interval-ms:10000}")
    public void checkAll() {
        instances.findAll().forEach(this::check);
    }
    private void check(BorneInstance instance) {
        long start = System.nanoTime();
        TechnicalHealthStatus health;
        String error = null;
        try {
            var container = docker.inspectBorneContainer(instance.getContainerId());
            if (!"running".equalsIgnoreCase(container.status())) {
                health = TechnicalHealthStatus.STOPPED;
            } else if (container.health() != null && !"healthy".equalsIgnoreCase(container.health())) {
                health = TechnicalHealthStatus.UNHEALTHY;
                error = "Docker health is " + container.health();
            } else {
                var response = http.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://" + instance.getContainerName() + ":8080/actuator/health/readiness"))
                        .timeout(Duration.ofSeconds(3)).GET().build(), HttpResponse.BodyHandlers.discarding());
                health = response.statusCode() == 200 ? TechnicalHealthStatus.HEALTHY : TechnicalHealthStatus.UNHEALTHY;
                if (health != TechnicalHealthStatus.HEALTHY) error = "Readiness returned HTTP " + response.statusCode();
            }
        } catch (Exception exception) {
            error = exception.getMessage();
            health = error != null && error.contains("No such container")
                    ? TechnicalHealthStatus.STOPPED : TechnicalHealthStatus.UNREACHABLE;
        }
        int failures = health == TechnicalHealthStatus.HEALTHY ? 0 : instance.getConsecutiveFailures() + 1;
        TechnicalHealthStatus effective = health == TechnicalHealthStatus.HEALTHY || health == TechnicalHealthStatus.STOPPED
                || failures >= failureThreshold ? health : instance.getApplicationHealthStatus();
        instance.update(instance.getContainerId(), effective == TechnicalHealthStatus.HEALTHY
                ? ContainerStatus.RUNNING : (health == TechnicalHealthStatus.STOPPED ? ContainerStatus.EXITED : instance.getContainerStatus()),
                effective, failures, error);
        instances.save(instance);
        history.save(new HealthHistory(instance.getBorneId(), instance.getContainerId(), effective,
                (System.nanoTime() - start) / 1_000_000, error));
    }
}
