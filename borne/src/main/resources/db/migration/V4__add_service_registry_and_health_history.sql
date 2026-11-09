CREATE TABLE borne_instance (
    id BIGSERIAL PRIMARY KEY,
    borne_id VARCHAR(100) NOT NULL UNIQUE,
    container_id VARCHAR(128),
    container_name VARCHAR(128) NOT NULL UNIQUE,
    image_name VARCHAR(255) NOT NULL,
    internal_port INTEGER NOT NULL DEFAULT 8080,
    container_status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    application_health_status VARCHAR(32) NOT NULL DEFAULT 'STARTING',
    last_health_check TIMESTAMP WITH TIME ZONE,
    consecutive_failures INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_error VARCHAR(1000)
);

CREATE TABLE borne_health_history (
    id BIGSERIAL PRIMARY KEY,
    borne_id VARCHAR(100) NOT NULL,
    container_id VARCHAR(128),
    health_status VARCHAR(32) NOT NULL,
    checked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response_time_ms BIGINT,
    error_message VARCHAR(1000)
);

CREATE INDEX borne_health_history_borne_time_idx
    ON borne_health_history(borne_id, checked_at DESC);
