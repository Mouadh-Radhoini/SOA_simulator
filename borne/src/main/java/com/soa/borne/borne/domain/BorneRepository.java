package com.soa.borne.borne.domain;

import java.util.List;
import java.util.Optional;

public interface BorneRepository {
    List<Borne> findAll();
    Optional<Borne> findById(String id);
    Optional<Borne> findByIdForUpdate(String id);
    Borne save(Borne borne);
    boolean existsById(String id);
}
