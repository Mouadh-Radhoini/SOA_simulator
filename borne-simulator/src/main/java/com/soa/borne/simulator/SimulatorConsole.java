package com.soa.borne.simulator;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class SimulatorConsole {
    private final BorneProcessManager manager;
    private final Scanner input;
    private final PrintStream output;
    public SimulatorConsole(BorneProcessManager manager) { this(manager, new Scanner(System.in), System.out); }
    SimulatorConsole(BorneProcessManager manager, Scanner input, PrintStream output) {
        this.manager = manager; this.input = input; this.output = output;
    }
    public void run() {
        output.println("BORNE SIMULATOR - every Borne provides electricity and water");
        output.println("1. Launch Bornes  2. Show status  3. Stop all  0. Exit");
        while (input.hasNextLine()) {
            output.print("> ");
            switch (input.nextLine().trim()) {
                case "1" -> launch();
                case "2" -> manager.status().forEach(p -> output.printf("%s | port %d | %s%n",
                        p.id(), p.port(), p.isAlive() ? "RUNNING" : "STOPPED"));
                case "3" -> { manager.shutdownAll(); output.println("All bornes stopped."); }
                case "0" -> { manager.shutdownAll(); return; }
                default -> output.println("Choose 1, 2, 3, or 0.");
            }
        }
        manager.shutdownAll();
    }
    private void launch() {
        try {
            int count = manager.configuredCount();
            while (count < 1) {
                output.print("How many bornes do you want to start? ");
                if (!input.hasNextLine()) return;
                try { count = Integer.parseInt(input.nextLine().trim()); }
                catch (NumberFormatException e) { count = 0; }
                if (count < 1 || count > 100) { output.println("Enter a number from 1 to 100."); count = 0; }
            }
            manager.launchBornes(count);
            output.println("Bornes launched; each provides electricity and water.");
        } catch (IOException | RuntimeException e) { output.println("Could not launch bornes: " + e.getMessage()); }
    }
}
