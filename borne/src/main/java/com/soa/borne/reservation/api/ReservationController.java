package com.soa.borne.reservation.api;

import com.soa.borne.borne.domain.Reservation;
import com.soa.borne.reservation.application.ReservationService;
import com.soa.borne.borne.api.dto.ConsumeRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ReservationController {
    private final ReservationService service;
    public ReservationController(ReservationService service) { this.service = service; }
    @PostMapping("/bornes/{borneId}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@PathVariable String borneId, @RequestBody @Valid CreateReservationRequest request) {
        return ReservationResponse.from(service.create(request.shipId(), borneId));
    }
    @GetMapping("/reservations/{id}") public ReservationResponse get(@PathVariable UUID id) { return ReservationResponse.from(service.find(id)); }
    @PostMapping("/reservations/{id}/start") public ReservationResponse start(@PathVariable UUID id) { return ReservationResponse.from(service.start(id)); }
    @PostMapping("/reservations/{id}/consumption") public ReservationResponse consume(@PathVariable UUID id, @RequestBody @Valid ConsumeRequest request) {
        return ReservationResponse.from(service.consume(id, request.electricityKwh(), request.waterLiters()));
    }
    @PostMapping("/reservations/{id}/complete") public ReservationResponse complete(@PathVariable UUID id) { return ReservationResponse.from(service.complete(id)); }
    @PostMapping("/reservations/{id}/cancel") public ReservationResponse cancel(@PathVariable UUID id) { return ReservationResponse.from(service.cancel(id)); }
    public record CreateReservationRequest(@NotBlank String shipId) {}
    public record ReservationResponse(UUID id, String shipId, String borneId, String status,
                                      BigDecimal electricityKwh, BigDecimal waterLiters,
                                      BigDecimal electricityCost, BigDecimal waterCost, BigDecimal totalCost) {
        static ReservationResponse from(Reservation r) {
            return new ReservationResponse(r.getId(), r.getShip().getId(), r.getBorne().getId(), r.getStatus().name(),
                    r.getElectricityKwh(), r.getWaterLiters(), r.getElectricityCost(), r.getWaterCost(), r.getTotalCost());
        }
    }
}
