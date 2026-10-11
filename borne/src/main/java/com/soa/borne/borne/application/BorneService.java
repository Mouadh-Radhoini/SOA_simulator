package com.soa.borne.borne.application;

import com.soa.borne.borne.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BorneService {
    private final BorneRepository repository;
    public BorneService(BorneRepository repository) { this.repository = repository; }
    @Transactional(readOnly = true) public List<Borne> findAll() { return repository.findAll(); }
    @Transactional(readOnly = true) public Borne findById(String id) {
        return repository.findById(id).orElseThrow(() -> new BorneNotFoundException(id));
    }
    @Transactional public Borne create(String id, double powerKw, double flowRate) {
        if (repository.existsById(id)) throw new IllegalArgumentException("A borne with id '" + id + "' already exists");
        return repository.save(new Borne(id, powerKw, flowRate));
    }
    @Transactional public Borne markOutOfService(String id) { return transition(id, Borne::markOutOfService); }
    @Transactional public Borne reactivate(String id) { return transition(id, Borne::reactivate); }
    private Borne transition(String id, java.util.function.Consumer<Borne> action) {
        Borne borne = repository.findByIdForUpdate(id).orElseThrow(() -> new BorneNotFoundException(id));
        action.accept(borne);
        return repository.save(borne);
    }
}
