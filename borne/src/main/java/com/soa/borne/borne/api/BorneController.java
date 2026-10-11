package com.soa.borne.borne.api;

import com.soa.borne.borne.api.dto.*;
import com.soa.borne.borne.application.BorneService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bornes")
public class BorneController {
    private final BorneService service;
    public BorneController(BorneService service) { this.service = service; }
    @GetMapping public List<BorneResponse> all() { return service.findAll().stream().map(BorneResponse::from).toList(); }
    @GetMapping("/{id}") public BorneResponse get(@PathVariable String id) { return BorneResponse.from(service.findById(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public BorneResponse create(@RequestBody @jakarta.validation.Valid CreateBorneRequest request) {
        return BorneResponse.from(service.create(request.id(), request.powerKw(), request.waterFlowLitersPerMinute()));
    }
    @PostMapping("/{id}/out-of-service") public BorneResponse out(@PathVariable String id) {
        return BorneResponse.from(service.markOutOfService(id));
    }
    @PostMapping("/{id}/reactivate") public BorneResponse reactivate(@PathVariable String id) {
        return BorneResponse.from(service.reactivate(id));
    }
}
