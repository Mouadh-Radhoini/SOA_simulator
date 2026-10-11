package com.soa.borne.ship.api;

import com.soa.borne.borne.application.ShipService;
import com.soa.borne.borne.domain.Ship;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/ships")
public class ShipController {
    private final ShipService service;
    public ShipController(ShipService service) { this.service = service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ShipResponse register(@RequestBody @Valid RegisterShipRequest request) {
        return ShipResponse.from(service.register(request.id(), request.name()));
    }
    @GetMapping("/{id}") public ShipResponse get(@PathVariable String id) { return ShipResponse.from(service.find(id)); }
    @GetMapping public List<ShipResponse> all() { return service.findAll().stream().map(ShipResponse::from).toList(); }
    public record RegisterShipRequest(@NotBlank String id, @NotBlank String name) {}
    public record ShipResponse(String id, String name, Instant createdAt) {
        static ShipResponse from(Ship ship) { return new ShipResponse(ship.getId(), ship.getName(), ship.getCreatedAt()); }
    }
}
