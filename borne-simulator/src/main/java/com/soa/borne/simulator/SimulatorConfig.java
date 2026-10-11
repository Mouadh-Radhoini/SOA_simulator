package com.soa.borne.simulator;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public record SimulatorConfig(
        int count,
        String borneIdPrefix,
        URI apiBaseUrl,
        int maxWorkers,
        Duration interval,
        Duration requestTimeout,
        int maxRetries,
        double powerKw,
        double waterFlowLitersPerMinute) {

    public SimulatorConfig {
        if (count < 0 || count > 100) throw new IllegalArgumentException("count must be between 0 and 100");
        if (borneIdPrefix == null || borneIdPrefix.isBlank()) throw new IllegalArgumentException("borne id prefix must not be blank");
        if (apiBaseUrl == null || !apiBaseUrl.isAbsolute()) throw new IllegalArgumentException("api URL must be absolute");
        if (maxWorkers < 1 || maxWorkers > 100) throw new IllegalArgumentException("max workers must be between 1 and 100");
        if (interval == null || interval.isNegative() || interval.isZero()) throw new IllegalArgumentException("interval must be positive");
        if (requestTimeout == null || requestTimeout.isNegative() || requestTimeout.isZero()) throw new IllegalArgumentException("request timeout must be positive");
        if (maxRetries < 0 || maxRetries > 5) throw new IllegalArgumentException("max retries must be between 0 and 5");
        if (!Double.isFinite(powerKw) || powerKw <= 0) throw new IllegalArgumentException("power must be positive");
        if (!Double.isFinite(waterFlowLitersPerMinute) || waterFlowLitersPerMinute <= 0) throw new IllegalArgumentException("water flow must be positive");
    }

    public String borneId(int index) {
        if (index < 0) throw new IndexOutOfBoundsException("borne index: " + index);
        return String.format("%s%03d", borneIdPrefix, index + 1);
    }

    public SimulatorConfig withCount(int requestedCount) {
        return new SimulatorConfig(requestedCount, borneIdPrefix, apiBaseUrl, maxWorkers,
                interval, requestTimeout, maxRetries, powerKw, waterFlowLitersPerMinute);
    }

    public static SimulatorConfig from(String[] args) {
        Map<String, String> values = new HashMap<>();
        for (String argument : args) {
            if (!argument.startsWith("--") || !argument.contains("=")) {
                throw new IllegalArgumentException("Arguments must use --key=value syntax: " + argument);
            }
            int separator = argument.indexOf('=');
            values.put(argument.substring(2, separator), argument.substring(separator + 1));
        }
        int count = integer(values, "count", 0);
        int maxWorkers = integer(values, "max-workers", Math.max(1, Math.min(count == 0 ? 10 : count, 10)));
        return new SimulatorConfig(
                count,
                values.getOrDefault("borne-id-prefix", "B"),
                URI.create(values.getOrDefault("api-url", "http://localhost:8080")),
                maxWorkers,
                Duration.ofMillis(integer(values, "interval-ms", 1000)),
                Duration.ofMillis(integer(values, "request-timeout-ms", 2000)),
                integer(values, "max-retries", 2),
                decimal(values, "power-kw", 7.2),
                decimal(values, "water-flow-lpm", 12));
    }

    private static int integer(Map<String, String> values, String key, int fallback) {
        try {
            return Integer.parseInt(values.getOrDefault(key, Integer.toString(fallback)));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid integer for --" + key, exception);
        }
    }

    private static double decimal(Map<String, String> values, String key, double fallback) {
        try {
            return Double.parseDouble(values.getOrDefault(key, Double.toString(fallback)));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid number for --" + key, exception);
        }
    }
}
