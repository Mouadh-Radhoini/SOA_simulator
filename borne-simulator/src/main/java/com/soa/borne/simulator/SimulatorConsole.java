package com.soa.borne.simulator;

import java.io.IOException;
import java.io.PrintStream;
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
        output.println("Borne simulator");
        output.println("1) launch configured bornes  2) status  3) stop all  0) exit");
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
            manager.launchConfiguredBornes();
            output.println("Bornes launched.");
        } catch (IOException | RuntimeException exception) {
            output.println("Could not launch bornes: " + exception.getMessage());
            manager.shutdownAll();
        }
    }

    private void printStatus() {
        if (manager.status().isEmpty()) {
            output.println("No bornes launched.");
            return;
        }
        manager.status().forEach(process -> output.printf(
                "%s on port %d: %s%n",
                process.id(), process.port(), process.isAlive() ? "RUNNING" : "STOPPED"));
    }
}
