package com.soa.borne.simulator;

import java.nio.file.Path;

public final class BorneSimulatorApplication {

    private BorneSimulatorApplication() {
    }

    public static void main(String[] args) {
        SimulatorConfiguration configuration = SimulatorConfiguration.from(args);
        BorneProcessManager manager = new BorneProcessManager(configuration);
        Runtime.getRuntime().addShutdownHook(new Thread(manager::shutdownAll, "borne-simulator-shutdown"));
        new SimulatorConsole(manager).run();
    }
}
