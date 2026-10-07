package com.soa.borne.infrastructure.persistence;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorneRepository extends JpaRepository<Borne, String> {

    List<Borne> findByStatus(BorneStatus status);
}
