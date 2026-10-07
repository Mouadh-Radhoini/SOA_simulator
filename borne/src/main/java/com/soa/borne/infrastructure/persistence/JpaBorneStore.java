package com.soa.borne.infrastructure.persistence;

import com.soa.borne.application.BorneStore;
import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaBorneStore implements BorneStore {

    private final BorneRepository repository;

    public JpaBorneStore(BorneRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Borne> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Borne> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    @Override
    public List<Borne> findByStatus(BorneStatus status) {
        return repository.findByStatus(status);
    }

    @Override
    public Borne save(Borne borne) {
        return repository.save(borne);
    }
}
