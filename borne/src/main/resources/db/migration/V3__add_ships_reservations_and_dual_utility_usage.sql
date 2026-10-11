ALTER TABLE bornes ADD COLUMN electricity_kwh NUMERIC(19,6) NOT NULL DEFAULT 0;
ALTER TABLE bornes ADD COLUMN water_liters NUMERIC(19,3) NOT NULL DEFAULT 0;
UPDATE bornes SET water_liters = 0 WHERE water_liters IS NULL;
UPDATE bornes SET flow_rate_lpm = 12 WHERE flow_rate_lpm IS NULL;
ALTER TABLE bornes ALTER COLUMN flow_rate_lpm SET NOT NULL;
ALTER TABLE bornes DROP COLUMN type;
ALTER TABLE bornes DROP COLUMN consumption;

CREATE TABLE ships (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reservations (
    id UUID PRIMARY KEY,
    ship_id VARCHAR(100) NOT NULL REFERENCES ships(id),
    borne_id VARCHAR(100) NOT NULL REFERENCES bornes(id),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    electricity_kwh NUMERIC(19,6) NOT NULL DEFAULT 0,
    water_liters NUMERIC(19,3) NOT NULL DEFAULT 0,
    electricity_cost NUMERIC(19,6) NOT NULL DEFAULT 0,
    water_cost NUMERIC(19,6) NOT NULL DEFAULT 0,
    total_cost NUMERIC(19,6) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT reservations_status_check CHECK (status IN ('RESERVED', 'IN_USE', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT reservations_electricity_check CHECK (electricity_kwh >= 0),
    CONSTRAINT reservations_water_check CHECK (water_liters >= 0)
);

CREATE INDEX reservations_ship_idx ON reservations(ship_id);
CREATE INDEX reservations_borne_idx ON reservations(borne_id);
CREATE UNIQUE INDEX reservations_one_active_borne_idx ON reservations(borne_id)
    WHERE status IN ('RESERVED', 'IN_USE');
CREATE UNIQUE INDEX reservations_one_active_ship_idx ON reservations(ship_id)
    WHERE status IN ('RESERVED', 'IN_USE');
