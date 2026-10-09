package com.soa.borne.simulator;

public record ManagedBorneProcess(String id, String type, int port, Process process) {

    public boolean isAlive() {
        return process.isAlive();
    }

    public void shutdown() {
        if (!process.isAlive()) {
            return;
        }
        process.destroy();
        try {
            if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            }
        } catch (InterruptedException exception) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
        }
    }
}
