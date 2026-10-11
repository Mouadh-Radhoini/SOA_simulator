package com.soa.borne.simulator;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public final class SimulationManager implements AutoCloseable {
    private final SimulatorConfig config;
    private final BorneApiClient apiClient;
    private final ExecutorService executor;
    private final AtomicBoolean submitted = new AtomicBoolean();
    private final List<BorneSimulationTask> tasks = new ArrayList<>();
    private final List<Future<?>> futures = new ArrayList<>();

    public SimulationManager(SimulatorConfig config, BorneApiClient apiClient) {
        this.config = config;
        this.apiClient = apiClient;
        this.executor = Executors.newFixedThreadPool(config.maxWorkers(), new SimulatorThreadFactory());
    }

    public synchronized List<BorneSimulationTask> start() throws IOException, InterruptedException {
        return start(config.count());
    }

    public synchronized List<BorneSimulationTask> start(int requestedCount) throws IOException, InterruptedException {
        if (requestedCount < 1 || requestedCount > 100) throw new IllegalArgumentException("count must be between 1 and 100");
        if (!submitted.compareAndSet(false, true)) throw new IllegalStateException("Simulation has already been started");
        try {
            if (!apiClient.isAvailable()) throw new IOException("Borne API is unavailable at " + config.apiBaseUrl());
        } catch (IOException | InterruptedException exception) {
            submitted.set(false);
            throw exception;
        }
        SimulatorConfig effectiveConfig = config.withCount(requestedCount);
        tasks.addAll(IntStream.range(0, requestedCount)
                .mapToObj(index -> new BorneSimulationTask(effectiveConfig.borneId(index), apiClient, effectiveConfig))
                .toList());
        for (BorneSimulationTask task : tasks) futures.add(executor.submit(task));
        return List.copyOf(tasks);
    }

    public synchronized Map<String, BorneSimulationTask.State> status() {
        return tasks.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                BorneSimulationTask::borneId, BorneSimulationTask::state));
    }

    public void stop() {
        executor.shutdownNow();
        tasks.forEach(BorneSimulationTask::stop);
        try {
            if (!executor.awaitTermination(Duration.ofSeconds(5).toMillis(), TimeUnit.MILLISECONDS)) {
                System.err.println("Some simulation tasks did not terminate within 5 seconds");
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() { stop(); }

    private static final class SimulatorThreadFactory implements ThreadFactory {
        private int sequence;
        @Override public synchronized Thread newThread(Runnable runnable) {
            return new Thread(runnable, "borne-simulation-" + (++sequence));
        }
    }
}
