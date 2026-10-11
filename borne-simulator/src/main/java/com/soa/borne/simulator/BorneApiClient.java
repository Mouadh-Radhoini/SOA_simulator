package com.soa.borne.simulator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public final class BorneApiClient {
    private final HttpClient client;
    private final URI baseUrl;
    private final Duration timeout;
    private final int maxRetries;

    public BorneApiClient(URI baseUrl, Duration timeout, int maxRetries) {
        this(HttpClient.newBuilder().connectTimeout(timeout).build(), baseUrl, timeout, maxRetries);
    }

    BorneApiClient(HttpClient client, URI baseUrl, Duration timeout, int maxRetries) {
        this.client = Objects.requireNonNull(client);
        this.baseUrl = normalize(Objects.requireNonNull(baseUrl));
        this.timeout = Objects.requireNonNull(timeout);
        this.maxRetries = maxRetries;
    }

    public boolean isAvailable() throws IOException, InterruptedException {
        return send("GET", "/api/bornes", null).statusCode() < 500;
    }

    public void ensureBorne(String id, double powerKw, double waterFlowLitersPerMinute)
            throws IOException, InterruptedException {
        HttpResponse<String> existing = send("GET", "/api/bornes/" + encode(id), null);
        if (existing.statusCode() == 200) return;
        if (existing.statusCode() != 404) {
            throw new IOException("Cannot inspect Borne " + id + ": HTTP " + existing.statusCode());
        }
        String body = "{\"id\":\"" + json(id) + "\",\"powerKw\":" + powerKw
                + ",\"waterFlowLitersPerMinute\":" + waterFlowLitersPerMinute + "}";
        HttpResponse<String> created = send("POST", "/api/bornes", body);
        if (created.statusCode() != 201 && created.statusCode() != 409) {
            throw new IOException("Cannot register Borne " + id + ": HTTP " + created.statusCode());
        }
    }

    public BorneSnapshot getBorne(String id) throws IOException, InterruptedException {
        HttpResponse<String> response = send("GET", "/api/bornes/" + encode(id), null);
        if (response.statusCode() == 404) throw new IOException("Borne " + id + " was not found");
        if (response.statusCode() != 200) throw new IOException("Cannot query Borne " + id + ": HTTP " + response.statusCode());
        String body = response.body();
        return new BorneSnapshot(id, field(body, "status"));
    }

    private HttpResponse<String> send(String method, String path, String body)
            throws IOException, InterruptedException {
        IOException lastIo = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            HttpRequest.Builder builder = HttpRequest.newBuilder(baseUrl.resolve(path))
                    .timeout(timeout)
                    .header("Accept", "application/json");
            if (body == null) builder.method(method, HttpRequest.BodyPublishers.noBody());
            else builder.method(method, HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json");
            try {
                HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                if ("GET".equals(method) && response.statusCode() >= 500 && attempt < maxRetries) {
                    Thread.sleep(Math.min(250L * (attempt + 1), 1000L));
                    continue;
                }
                return response;
            } catch (IOException exception) {
                lastIo = exception;
                if (!"GET".equals(method) || attempt == maxRetries) throw exception;
                Thread.sleep(Math.min(250L * (attempt + 1), 1000L));
            }
        }
        throw lastIo == null ? new IOException("HTTP request failed") : lastIo;
    }

    private static URI normalize(URI uri) {
        String value = uri.toString();
        return URI.create(value.endsWith("/") ? value.substring(0, value.length() - 1) : value);
    }

    private static String encode(String value) {
        return value.replace("%", "%25").replace("/", "%2F");
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String field(String json, String field) throws IOException {
        String marker = "\"" + field + "\"";
        int start = json.indexOf(marker);
        if (start < 0) throw new IOException("Response did not contain " + field);
        int colon = json.indexOf(':', start + marker.length());
        int firstQuote = json.indexOf('"', colon + 1);
        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (colon < 0 || firstQuote < 0 || secondQuote < 0) throw new IOException("Invalid response field " + field);
        return json.substring(firstQuote + 1, secondQuote);
    }

    public record BorneSnapshot(String id, String status) {}
}
