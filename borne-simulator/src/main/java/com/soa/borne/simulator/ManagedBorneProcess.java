package com.soa.borne.simulator;

public record ManagedBorneProcess(String id, int port, Process process) {
    public boolean isAlive() { return process.isAlive(); }
    public void shutdown() {
        if (!process.isAlive()) return;
        process.destroy();
        try {
            if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) process.destroyForcibly();
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
        }
    }
}
