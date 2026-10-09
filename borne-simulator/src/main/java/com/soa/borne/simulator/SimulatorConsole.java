package com.soa.borne.simulator;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SimulatorConsole {

    private final BorneProcessManager manager;
    private final Scanner input;
    private final PrintStream output;

    public SimulatorConsole(BorneProcessManager manager) {
        this(manager, new Scanner(System.in), System.out);
    }

    SimulatorConsole(BorneProcessManager manager, Scanner input, PrintStream output) {
        this.manager = manager;
        this.input = input;
        this.output = output;
    }

    public void run() {
        output.println("================================");
        output.println("       BORNE SIMULATOR");
        output.println("================================");
        output.println("1. Configure and launch bornes");
        output.println("2. Show bornes status");
        output.println("3. Stop all bornes");
        output.println("0. Exit");
        while (input.hasNextLine()) {
            output.print("> ");
            switch (input.nextLine().trim()) {
                case "1" -> launch();
                case "2" -> printStatus();
                case "3" -> {
                    manager.shutdownAll();
                    output.println("All bornes stopped.");
                }
                case "0" -> {
                    manager.shutdownAll();
                    return;
                }
                default -> output.println("Choose 1, 2, 3, or 0.");
            }
        }
        manager.shutdownAll();
    }

    private void launch() {
        try {
            int count = manager.configuredCount();
            if (count == 0) {
                count = readCount();
            }
            List<String> types = readTypes(count);
            output.println("Bornes configuration:");
            for (int index = 0; index < count; index++) {
                output.printf("%s -> %s -> port %d%n", manager.borneId(index), types.get(index),
                        manager.port(index));
            }
            output.print("Launch these bornes? (y/n) ");
            if (!input.hasNextLine() || !input.nextLine().trim().equalsIgnoreCase("y")) {
                return;
            }
            manager.launchBornes(count, types);
            output.println("Bornes launched successfully.");
        } catch (IOException | RuntimeException exception) {
            output.println("Could not launch bornes: " + exception.getMessage());
        }
    }

    private int readCount() {
        while (true) {
            output.print("How many bornes do you want to create? ");
            if (!input.hasNextLine()) {
                throw new IllegalStateException("No borne count was provided");
            }
            try {
                int count = Integer.parseInt(input.nextLine().trim());
                if (count > 0) {
                    return count;
                }
            } catch (NumberFormatException ignored) {
                // Keep prompting until the interactive input is valid.
            }
            output.println("Enter a positive whole number.");
        }
    }

    private List<String> readTypes(int count) {
        List<String> types = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            output.printf("%n--- Borne %d ---%n1. ELECTRICITY%n2. WATER%n", index + 1);
            while (true) {
                output.print("> ");
                if (!input.hasNextLine()) {
                    throw new IllegalStateException("No borne type was provided");
                }
                String value = input.nextLine().trim();
                if ("1".equals(value) || "ELECTRICITY".equalsIgnoreCase(value)) {
                    types.add("ELECTRICITY");
                    break;
                }
                if ("2".equals(value) || "WATER".equalsIgnoreCase(value)) {
                    types.add("WATER");
                    break;
                }
                output.println("Choose 1 for ELECTRICITY or 2 for WATER.");
            }
        }
        return types;
    }

    private void printStatus() {
        if (manager.status().isEmpty()) {
            output.println("No bornes launched.");
            return;
        }
        manager.status().forEach(process -> output.printf(
                "%s | %s | %d | %s%n",
                process.id(), process.type(), process.port(),
                process.isAlive() ? "RUNNING" : "STOPPED"));
    }
}
