package com.soa.borne.borne.application;

import com.soa.borne.borne.domain.Ship;
import com.soa.borne.ship.infrastructure.persistence.JpaShipRepository;
import com.soa.borne.shared.error.ShipNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShipService {
    private final JpaShipRepository repository;
    public ShipService(JpaShipRepository repository) { this.repository = repository; }
    @Transactional public Ship register(String id, String name) {
        if (repository.existsById(id)) throw new IllegalArgumentException("A ship with id '" + id + "' already exists");
        return repository.save(new Ship(id, name));
    }
    @Transactional(readOnly = true) public Ship find(String id) {
        return repository.findById(id).orElseThrow(() -> new ShipNotFoundException(id));
    }
    @Transactional(readOnly = true) public List<Ship> findAll() { return repository.findAll(); }
}
