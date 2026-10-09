ALTER TABLE bornes ADD COLUMN type VARCHAR(32) NOT NULL DEFAULT 'ELECTRICITY';
ALTER TABLE bornes ADD COLUMN consumption DOUBLE PRECISION NOT NULL DEFAULT 0;
ALTER TABLE bornes ADD COLUMN flow_rate_lpm DOUBLE PRECISION;

UPDATE bornes SET consumption = energy_kwh;

ALTER TABLE bornes ALTER COLUMN power_kw DROP NOT NULL;
ALTER TABLE bornes DROP COLUMN energy_kwh;
ALTER TABLE bornes DROP COLUMN battery_capacity_kwh;
ALTER TABLE bornes DROP COLUMN charging_started_at;
ALTER TABLE bornes DROP COLUMN last_energy_update_at;
