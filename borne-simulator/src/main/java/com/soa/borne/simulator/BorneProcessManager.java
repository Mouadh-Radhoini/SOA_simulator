package com.soa.borne.simulator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BorneProcessManager {

    @FunctionalInterface
    interface ProcessStarter {
        Process start(List<String> command) throws IOException;
    }

    private final SimulatorConfiguration configuration;
    private final ProcessStarter processStarter;
    private final List<ManagedBorneProcess> processes = new ArrayList<>();

    public BorneProcessManager(SimulatorConfiguration configuration) {
        this(configuration, command -> new ProcessBuilder(command)
                .inheritIO()
                .start());
    }

    BorneProcessManager(SimulatorConfiguration configuration, ProcessStarter processStarter) {
        this.configuration = configuration;
        this.processStarter = processStarter;
    }

    public synchronized List<ManagedBorneProcess> launchConfiguredBornes() throws IOException {
        return launchBornes(configuration.count(),
                java.util.Collections.nCopies(configuration.count(), "ELECTRICITY"));
    }

    public synchronized List<ManagedBorneProcess> launchBornes(int count, List<String> types)
            throws IOException {
        if (count < 1 || types.size() != count) {
            throw new IllegalArgumentException("A type is required for every borne");
        }
        if (configuration.basePort() > 65535 - count + 1) {
            throw new IllegalArgumentException("Base port does not leave room for all processes");
        }
        if (!processes.isEmpty()) {
            return List.copyOf(processes);
        }
        for (int index = 0; index < count; index++) {
            int port = configuration.basePort() + index;
            String id = String.format("%s%03d", configuration.borneIdPrefix(), index + 1);
            if (!isPortAvailable(port)) {
                throw new IOException("Port " + port + " is already in use");
            }
            List<String> command = List.of(
                    configuration.javaCommand(),
                    "-jar",
                    configuration.borneJar().toString(),
                    "--server.port=" + port,
                    "--borne.id=" + id,
                    "--borne.type=" + types.get(index));
            processes.add(new ManagedBorneProcess(id, types.get(index), port,
                    processStarter.start(command)));
        }

        return List.copyOf(processes);
    }

    public int port(int index) {
        return configuration.basePort() + index;
    }

    public String borneId(int index) {
        return String.format("%s%03d", configuration.borneIdPrefix(), index + 1);
    }

    public int configuredCount() {
        return configuration.count();
    }

    private boolean isPortAvailable(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 100);
            return false;
        } catch (IOException exception) {
            return true;
        }
    }

    public synchronized List<ManagedBorneProcess> status() {
        return processes.stream()
                .sorted(Comparator.comparingInt(ManagedBorneProcess::port))
                .toList();
    }

    public synchronized void shutdownAll() {
        processes.forEach(ManagedBorneProcess::shutdown);
        processes.clear();
    }
}
