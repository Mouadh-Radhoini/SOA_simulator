package com.soa.borne.simulator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BorneProcessManager {
    @FunctionalInterface interface ProcessStarter { Process start(List<String> command) throws IOException; }
    private final SimulatorConfiguration configuration;
    private final ProcessStarter processStarter;
    private final List<ManagedBorneProcess> processes = new ArrayList<>();
    public BorneProcessManager(SimulatorConfiguration configuration) {
        this(configuration, command -> new ProcessBuilder(command).inheritIO().start());
    }
    BorneProcessManager(SimulatorConfiguration configuration, ProcessStarter processStarter) {
        this.configuration = configuration; this.processStarter = processStarter;
    }
    public synchronized List<ManagedBorneProcess> launchConfiguredBornes() throws IOException {
        int count = configuration.count();
        if (count < 1) throw new IllegalArgumentException("Count must be positive");
        return launchBornes(count);
    }
    public synchronized List<ManagedBorneProcess> launchBornes(int count) throws IOException {
        if (count < 1) throw new IllegalArgumentException("Count must be positive");
        if (configuration.basePort() > 65535 - count + 1) throw new IllegalArgumentException("Base port does not leave room for all processes");
        if (!processes.isEmpty()) return List.copyOf(processes);
        List<ManagedBorneProcess> started = new ArrayList<>();
        try {
            for (int index = 0; index < count; index++) {
                int port = configuration.basePort() + index;
                if (!isPortAvailable(port)) throw new IOException("Port " + port + " is already in use");
                String id = borneId(index);
                List<String> command = List.of(configuration.javaCommand(), "-jar",
                        configuration.borneJar().toString(), "--server.port=" + port, "--borne.id=" + id);
                started.add(new ManagedBorneProcess(id, port, processStarter.start(command)));
            }
            processes.addAll(started);
            return List.copyOf(processes);
        } catch (IOException | RuntimeException e) {
            started.forEach(ManagedBorneProcess::shutdown);
            throw e;
        }
    }
    public int port(int index) { return configuration.basePort() + index; }
    public String borneId(int index) { return String.format("%s%03d", configuration.borneIdPrefix(), index + 1); }
    public int configuredCount() { return configuration.count(); }
    private boolean isPortAvailable(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 100);
            return false;
        } catch (IOException e) { return true; }
    }
    public synchronized List<ManagedBorneProcess> status() {
        return processes.stream().sorted(Comparator.comparingInt(ManagedBorneProcess::port)).toList();
    }
    public synchronized void shutdownAll() { processes.forEach(ManagedBorneProcess::shutdown); processes.clear(); }
}
