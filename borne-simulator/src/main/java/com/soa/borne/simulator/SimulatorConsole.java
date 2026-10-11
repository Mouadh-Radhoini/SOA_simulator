package com.soa.borne.simulator;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

public final class SimulatorConsole {
    private final SimulationManager manager;
    private final SimulatorConfig config;
    private final Scanner input;
    private final PrintStream output;

    public SimulatorConsole(SimulationManager manager, SimulatorConfig config) {
        this(manager, config, new Scanner(System.in), System.out);
    }

    SimulatorConsole(SimulationManager manager, SimulatorConfig config, Scanner input, PrintStream output) {
        this.manager = manager;
        this.config = config;
        this.input = input;
        this.output = output;
    }

    public void run() {
        output.println("=== Port Borne Simulator ===");
        output.println("API URL: " + config.apiBaseUrl());
        int count = config.count() > 0 ? config.count() : readCount();
        output.println("Created simulation tasks:");
        for (int index = 0; index < count; index++) output.println(config.borneId(index));
        try {
            manager.start(count);
            output.println("Starting thread-based simulation...");
            output.println("Commands: status, stop, exit");
            while (input.hasNextLine()) {
                output.print("> ");
                switch (input.nextLine().trim().toLowerCase()) {
                    case "status" -> printStatus();
                    case "stop", "exit" -> { manager.stop(); return; }
                    default -> output.println("Use status, stop, or exit.");
                }
            }
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) Thread.currentThread().interrupt();
            output.println("Could not start simulation: " + exception.getMessage());
            manager.stop();
        }
    }

    private int readCount() {
        while (true) {
            output.print("How many Bornes do you want to simulate? ");
            if (!input.hasNextLine()) throw new IllegalStateException("No Borne count was provided");
            try {
                int count = Integer.parseInt(input.nextLine().trim());
                if (count >= 1 && count <= 100) return count;
            } catch (NumberFormatException ignored) {
                // Prompt again with a clear validation message.
            }
            output.println("Enter a number from 1 to 100.");
        }
    }

    private void printStatus() {
        for (Map.Entry<String, BorneSimulationTask.State> entry : manager.status().entrySet()) {
            output.println(entry.getKey() + ": " + entry.getValue().name().toLowerCase());
        }
    }
}
