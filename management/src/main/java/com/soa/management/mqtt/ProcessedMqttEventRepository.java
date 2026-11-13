package com.soa.management.mqtt;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProcessedMqttEventRepository extends JpaRepository<ProcessedMqttEvent, UUID> {}
