package com.soa.borne.simulator;

import java.io.IOException;
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
        if (!processes.isEmpty()) {
            return List.copyOf(processes);
        }
        for (int index = 0; index < configuration.count(); index++) {
            int port = configuration.basePort() + index;
            String id = configuration.borneIdPrefix() + (index + 1);
            List<String> command = List.of(
                    configuration.javaCommand(),
                    "-jar",
                    configuration.borneJar().toString(),
                    "--server.port=" + port,
                    "--borne.id=" + id);
            processes.add(new ManagedBorneProcess(id, port, processStarter.start(command)));
        }
        return List.copyOf(processes);
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
