package com.soa.borne.simulator;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BorneSimulationTask implements Runnable {
    public enum State { CREATED, RUNNING, FAILED, STOPPED }
    private static final Logger LOGGER = Logger.getLogger(BorneSimulationTask.class.getName());
    private final String borneId;
    private final BorneApiClient apiClient;
    private final SimulatorConfig config;
    private final AtomicReference<State> state = new AtomicReference<>(State.CREATED);

    public BorneSimulationTask(String borneId, BorneApiClient apiClient, SimulatorConfig config) {
        this.borneId = Objects.requireNonNull(borneId);
        this.apiClient = Objects.requireNonNull(apiClient);
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public void run() {
        if (!state.compareAndSet(State.CREATED, State.RUNNING)) return;
        try {
            apiClient.ensureBorne(borneId, config.powerKw(), config.waterFlowLitersPerMinute());
            LOGGER.info(() -> borneId + ": running");
            while (!Thread.currentThread().isInterrupted()) {
                BorneApiClient.BorneSnapshot snapshot = apiClient.getBorne(borneId);
                LOGGER.fine(() -> borneId + ": " + snapshot.status());
                Thread.sleep(config.interval());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            state.set(State.STOPPED);
        } catch (IOException | RuntimeException exception) {
            state.set(State.FAILED);
            LOGGER.log(Level.WARNING, borneId + ": simulation failed: " + exception.getMessage(), exception);
        } finally {
            state.compareAndSet(State.RUNNING, State.STOPPED);
        }
    }

    public String borneId() { return borneId; }
    public State state() { return state.get(); }
    public void stop() {
        state.updateAndGet(current -> current == State.CREATED ? State.STOPPED : current);
    }
}
