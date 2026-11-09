package com.soa.management.registry;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BorneInstanceRepository extends JpaRepository<BorneInstance, Long> {
    Optional<BorneInstance> findByBorneId(String borneId);
    boolean existsByBorneId(String borneId);
}
