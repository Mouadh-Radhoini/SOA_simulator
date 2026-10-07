package com.soa.borne.application;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneStatus;

import java.util.List;
import java.util.Optional;

public interface BorneStore {

    List<Borne> findAll();

    Optional<Borne> findById(String id);

    boolean existsById(String id);

    List<Borne> findByStatus(BorneStatus status);

    Borne save(Borne borne);
}
