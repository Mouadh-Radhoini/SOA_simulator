package com.soa.borne.simulator;

public final class BorneSimulatorApplication {

    private BorneSimulatorApplication() {
    }

    public static void main(String[] args) {
        SimulatorConfig configuration = SimulatorConfig.from(args);
        SimulationManager manager = new SimulationManager(configuration,
                new BorneApiClient(configuration.apiBaseUrl(), configuration.requestTimeout(), configuration.maxRetries()));
        Runtime.getRuntime().addShutdownHook(new Thread(manager::stop, "borne-simulator-shutdown"));
        new SimulatorConsole(manager, configuration).run();
    }
}
