package com.soa.borne.simulator;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record SimulatorConfiguration(
        int count,
        int basePort,
        String borneIdPrefix,
        Path borneJar,
        String javaCommand) {

    public SimulatorConfiguration {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
        if (basePort < 1 || basePort > 65535 - count + 1) {
            throw new IllegalArgumentException("base port does not leave room for all processes");
        }
        if (borneIdPrefix == null || borneIdPrefix.isBlank()) {
            throw new IllegalArgumentException("borne id prefix must not be blank");
        }
    }

    public static SimulatorConfiguration from(String[] args) {
        Map<String, String> values = new HashMap<>();
        for (String argument : args) {
            if (argument.startsWith("--") && argument.contains("=")) {
                int separator = argument.indexOf('=');
                values.put(argument.substring(2, separator), argument.substring(separator + 1));
            }
        }
        return new SimulatorConfiguration(
                integer(values, "count", 3),
                integer(values, "base-port", 8081),
                values.getOrDefault("borne-id-prefix", "B"),
                Path.of(values.getOrDefault("borne-jar", "borne/target/borne-0.0.1-SNAPSHOT.jar")),
                values.getOrDefault("java", Path.of(System.getProperty("java.home"), "bin", "java").toString()));
    }

    private static int integer(Map<String, String> values, String key, int defaultValue) {
        return Integer.parseInt(values.getOrDefault(key, Integer.toString(defaultValue)));
    }
}
