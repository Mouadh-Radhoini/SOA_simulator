package com.soa.borne.borne.infrastructure.persistence;

import com.soa.borne.borne.domain.Borne;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaBorneRepository extends JpaRepository<Borne, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Borne b where b.id = :id")
    Optional<Borne> findByIdForUpdate(@Param("id") String id);
}
