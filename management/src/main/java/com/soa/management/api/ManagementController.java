package com.soa.management.api;

import com.soa.management.registry.BorneInstance;
import com.soa.management.service.BorneManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/management/bornes")
public class ManagementController {
    public record CreateRequest(@Min(1) @Max(100) int count) {}
    private final BorneManagementService service;
    public ManagementController(BorneManagementService service) { this.service = service; }
    @PostMapping @PreAuthorize("hasRole('ADMIN')") public List<BorneInstanceResponse> create(@RequestBody @Valid CreateRequest request) { return service.create(request.count()).stream().map(BorneInstanceResponse::from).toList(); }
    @GetMapping public List<BorneInstanceResponse> list() { return service.list().stream().map(BorneInstanceResponse::from).toList(); }
    @GetMapping("/{id}") public BorneInstanceResponse get(@PathVariable String id) { return BorneInstanceResponse.from(service.find(id)); }
    @PostMapping("/{id}/start") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void start(@PathVariable String id) { service.start(id); }
    @PostMapping("/{id}/stop") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void stop(@PathVariable String id) { service.stop(id); }
    @PostMapping("/{id}/restart") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void restart(@PathVariable String id) { service.restart(id); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void remove(@PathVariable String id) { service.remove(id); }
    @GetMapping("/{id}/health") public BorneInstanceResponse health(@PathVariable String id) { return BorneInstanceResponse.from(service.find(id)); }
    @GetMapping("/health/overall") public List<BorneInstanceResponse> overallHealth() {
        return service.list().stream().map(BorneInstanceResponse::from).toList();
    }
}
