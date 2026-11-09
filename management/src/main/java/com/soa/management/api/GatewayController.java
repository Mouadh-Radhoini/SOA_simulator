package com.soa.management.api;

import com.soa.management.service.BorneManagementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("/api/bornes/{borneId}")
public class GatewayController {
    private final BorneManagementService service;
    private final HttpClient client = HttpClient.newBuilder().build();
    public GatewayController(BorneManagementService service) { this.service = service; }

    @RequestMapping("/health")
    public ResponseEntity<byte[]> health(@PathVariable String borneId) {
        return forward(borneId, "/actuator/health");
    }
    @RequestMapping("/**")
    public ResponseEntity<byte[]> route(@PathVariable String borneId, HttpServletRequest request,
                                        @RequestBody(required = false) byte[] body) {
        String path = request.getRequestURI().substring(("/api/bornes/" + borneId).length());
        return forward(borneId, path.isBlank() ? "/" : path, request.getMethod(), body,
                request.getHeader("Content-Type"));
    }
    private ResponseEntity<byte[]> forward(String borneId, String path) {
        return forward(borneId, path, "GET", null, null);
    }
    private ResponseEntity<byte[]> forward(String borneId, String path, String method, byte[] body, String contentType) {
        var instance = service.find(borneId);
        if (instance.getApplicationHealthStatus() == com.soa.management.registry.TechnicalHealthStatus.STOPPED
                || instance.getApplicationHealthStatus() == com.soa.management.registry.TechnicalHealthStatus.UNREACHABLE) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Borne is unavailable".getBytes());
        }
        try {
            var builder = HttpRequest.newBuilder(URI.create("http://" + instance.getContainerName() + ":8080" + path))
                    .header("Accept", "application/json");
            if (contentType != null) builder.header("Content-Type", contentType);
            builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body));
            var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            return ResponseEntity.status(response.statusCode()).contentType(MediaType.APPLICATION_JSON).body(response.body());
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Borne gateway request failed".getBytes());
        }
    }
}
