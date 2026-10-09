# Borne platform

The platform models supply points (`borne`) and a local interactive
`borne-simulator`. A supply point is either `ELECTRICITY` (consumption in kWh)
or `WATER` (consumption in liters). The layered architecture remains:
web DTOs/controllers, application services, domain rules, and JPA/Flyway
infrastructure backed by PostgreSQL.

## Build and database

Requirements are JDK 21 and the supplied Maven wrapper.

```bash
docker compose up -d postgres
./mvnw clean package
```

PostgreSQL is exposed on port `5433` by Docker. Override `DB_URL`,
`DB_USERNAME`, and `DB_PASSWORD` when using another database. Flyway applies
`V2__refactor_borne_supply_point.sql` after the original migration without
rewriting the applied `V1` migration.

## REST service

Start a borne process with an ID, type, and port:

```bash
java -jar borne/target/borne-0.0.1-SNAPSHOT.jar \
  --borne.id=B001 --borne.type=ELECTRICITY --server.port=8081
```

Create and use supply points:

```bash
curl -X POST http://localhost:8081/api/bornes \
  -H 'Content-Type: application/json' \
  -d '{"id":"B002","type":"WATER","rate":12}'
curl -X POST http://localhost:8081/api/bornes/B001/consume \
  -H 'Content-Type: application/json' -d '{"amount":8}'
curl http://localhost:8081/api/bornes/B001/consumption
curl http://localhost:8081/api/bornes/B001/price
curl -X POST http://localhost:8081/api/bornes/B001/reserve
curl -X POST http://localhost:8081/api/bornes/B001/start
curl -X POST http://localhost:8081/api/bornes/B001/stop
```

The status lifecycle is `AVAILABLE -> RESERVED -> IN_USE -> AVAILABLE`.
Any active state can transition to `OUT_OF_SERVICE`, and an out-of-service
borne can be reactivated. Invalid transitions return `409`, unknown IDs return
`404`, and invalid request values return `400`.

Electricity pricing is progressive: 0–10 kWh at 0.30 DT/kWh, 10–20 kWh at
0.45 DT/kWh, then 0.60 DT/kWh. Water has independent configurable tariffs:
0–100 L at 0.002 DT/L, 100–500 L at 0.003 DT/L, then 0.005 DT/L. The values
are configured in `application.yaml` or with environment variables.

## Interactive simulator

Build and start it from the repository root:

```bash
java -jar borne-simulator/target/borne-simulator-0.0.1-SNAPSHOT.jar
```

The default menu asks for the number of bornes and then asks for each type.
Ports start at 8081 and IDs are generated as `B001`, `B002`, and so on. The
simulator launches each process with `ProcessBuilder`, passes
`--borne.id`, `--borne.type`, and `--server.port`, reports running status, checks
ports before launch, and gracefully stops child processes.

`--count` remains supported for non-interactive count selection:

```bash
java -jar borne-simulator/target/borne-simulator-0.0.1-SNAPSHOT.jar \
  --count=3 --base-port=8081 \
  --borne-jar=borne/target/borne-0.0.1-SNAPSHOT.jar
```

The simulator still asks for the three types, while `--base-port`,
`--borne-id-prefix`, `--borne-jar`, and `--java` customize launch settings.

## Tests

```bash
./mvnw test
./mvnw clean package
```

Tests cover both supply-point types, status transitions, consumption
validation, progressive electricity and water pricing, REST errors, database
persistence, simulator configuration, command construction, and process
management.
