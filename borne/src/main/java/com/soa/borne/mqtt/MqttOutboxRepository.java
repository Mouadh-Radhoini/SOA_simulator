package com.soa.borne.mqtt;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MqttOutboxRepository extends JpaRepository<MqttOutbox, Long> {
    List<MqttOutbox> findTop50ByPublishedFalseOrderByCreatedAtAsc();
}
