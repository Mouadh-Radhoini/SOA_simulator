package com.soa.borne.borne.infrastructure.persistence;

import com.soa.borne.borne.domain.Borne;
import com.soa.borne.borne.domain.BorneRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaBorneStore implements BorneRepository {
    private final JpaBorneRepository repository;
    public JpaBorneStore(JpaBorneRepository repository) { this.repository = repository; }
    public List<Borne> findAll() { return repository.findAll(); }
    public Optional<Borne> findById(String id) { return repository.findById(id); }
    public Optional<Borne> findByIdForUpdate(String id) { return repository.findByIdForUpdate(id); }
    public boolean existsById(String id) { return repository.existsById(id); }
    public Borne save(Borne borne) { return repository.save(borne); }
}
