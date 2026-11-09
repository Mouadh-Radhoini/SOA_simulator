package com.soa.management.registry;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface HealthHistoryRepository extends JpaRepository<HealthHistory, Long> {}
