package com.soa.borne.web;

import com.soa.borne.application.BorneService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/bornes")
public class BorneController {

    private final BorneService service;

    public BorneController(BorneService service) {
        this.service = service;
    }

    @GetMapping
    public List<BorneResponse> findAll() {
        return service.findAll().stream().map(BorneResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorneResponse create(@RequestBody @jakarta.validation.Valid CreateBorneRequest request) {
        return BorneResponse.from(service.create(
                request.id(), request.batteryCapacityKwh(), request.powerKw()));
    }

    @GetMapping("/{id}")
    public BorneResponse findById(@PathVariable @NotBlank String id) {
        return BorneResponse.from(service.findById(id));
    }

    @GetMapping("/{id}/status")
    public StatusResponse status(@PathVariable String id) {
        var borne = service.findById(id);
        return new StatusResponse(borne.getId(), borne.getStatus());
    }

    @GetMapping("/{id}/consumption")
    public ConsumptionResponse consumption(@PathVariable String id) {
        var borne = service.findById(id);
        return new ConsumptionResponse(borne.getId(), borne.getEnergyKwh(), borne.getPowerKw());
    }

    @PutMapping("/{id}/status")
    public BorneResponse updateStatus(@PathVariable String id,
                                      @RequestBody @jakarta.validation.Valid StatusUpdateRequest request) {
        return BorneResponse.from(service.updateStatus(id, request.status()));
    }

    @PostMapping("/{id}/reserve")
    public BorneResponse reserve(@PathVariable String id) {
        return BorneResponse.from(service.reserve(id));
    }

    @PostMapping("/{id}/release")
    public BorneResponse release(@PathVariable String id) {
        return BorneResponse.from(service.release(id));
    }

    @PostMapping("/{id}/start-charging")
    public BorneResponse startCharging(@PathVariable String id) {
        return BorneResponse.from(service.startCharging(id));
    }

    @PostMapping("/{id}/stop-charging")
    public BorneResponse stopCharging(@PathVariable String id) {
        return BorneResponse.from(service.stopCharging(id));
    }

    @PostMapping("/{id}/out-of-service")
    public BorneResponse markOutOfService(@PathVariable String id) {
        return BorneResponse.from(service.markOutOfService(id));
    }

    @PostMapping("/{id}/reactivate")
    public BorneResponse reactivate(@PathVariable String id) {
        return BorneResponse.from(service.reactivate(id));
    }
}
