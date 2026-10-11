# Port ship reservation simulator

This project is a software simulator for utility stations in a maritime port.
When a ship is docked, it can reserve a **Borne** and simulate electricity and
water service. It does not control physical pumps, cables, batteries, sensors,
or other equipment.

Every Borne provides **both** utilities. Electricity and water are separate
measurements and are billed independently; a Borne is never classified as
electricity-only or water-only.

## Architecture

The repository keeps two Maven applications:

- `borne/` is a Spring Boot REST service. Each process has its own HTTP port and
  Borne identifier, while all processes share PostgreSQL.
- `borne-simulator/` is a Java console process manager. It starts and stops
  Borne JVMs with `ProcessBuilder` and never accesses PostgreSQL directly.

The service is layered into web DTOs/controllers, application use cases,
domain entities and transitions, and infrastructure repositories/Flyway
migrations.

The database contains `bornes`, `ships`, and `reservations`. A reservation
references exactly one ship and one Borne. Reservation usage and finalized
electricity/water costs are persisted on the reservation; Borne-level totals
are only cumulative station counters.

## Reservation lifecycle

1. Register a ship.
2. Create a reservation for an available Borne.
3. Start the reservation.
4. Record electricity in kWh and water in liters.
5. Complete the reservation to calculate and retain final charges.
6. The Borne is released to `AVAILABLE`.

Reservations can also be cancelled while `RESERVED`. Bornes transition through
`AVAILABLE -> RESERVED -> IN_USE -> AVAILABLE`; `OUT_OF_SERVICE` Bornes cannot
be reserved and can only be reactivated. Database locking and PostgreSQL
partial unique indexes prevent two active reservations from claiming the same
Borne or ship.

## Pricing

Progressive marginal tariffs are configured in `borne/src/main/resources/application.yaml`
and can be overridden with environment variables:

| Utility | Tier 1 | Tier 2 | Tier 3 |
|---|---:|---:|---:|
| Electricity | first 10 kWh at 0.30 DT/kWh | next to 20 kWh at 0.45 | above 20 at 0.60 |
| Water | first 100 L at 0.002 DT/L | next to 500 L at 0.003 | above 500 at 0.005 |

For example, 14 kWh costs `4.80 DT`; 600 L costs `1.90 DT`. The reservation
total is electricity cost plus water cost. Calculations use `BigDecimal`.

## Running locally

Requirements: JDK 21 and the Maven wrapper. PostgreSQL is supplied on host
port `5433` by Docker Compose (container port `5432`):

```bash
docker compose up -d postgres
./mvnw clean package
```

Database settings can be overridden without committing secrets:
`DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Borne process settings are
`BORNE_ID`, `BORNE_POWER_KW`, `BORNE_WATER_FLOW_LPM`, and `SERVER_PORT`.
Flyway applies the existing V1/V2 history and then
`V3__add_ships_reservations_and_dual_utility_usage.sql`; applied migrations
are not rewritten.

## REST workflow

Start one Borne:

```bash
java -jar borne/target/borne-0.0.1-SNAPSHOT.jar \
  --borne.id=B001 --borne.power-kw=7.2 --borne.water-flow-lpm=12 \
  --server.port=8081
```

Register a ship and reserve the Borne:

```bash
curl -X POST http://localhost:8081/api/ships \
  -H 'Content-Type: application/json' \
  -d '{"id":"S001","name":"Atlantic Star"}'

curl -X POST http://localhost:8081/api/bornes/B001/reservations \
  -H 'Content-Type: application/json' \
  -d '{"shipId":"S001"}'
```

Use the returned reservation UUID:

```bash
curl -X POST http://localhost:8081/api/reservations/{reservationId}/start

curl -X POST http://localhost:8081/api/reservations/{reservationId}/consumption \
  -H 'Content-Type: application/json' \
  -d '{"electricityKwh":14,"waterLiters":600}'

curl -X POST http://localhost:8081/api/reservations/{reservationId}/complete
curl http://localhost:8081/api/reservations/{reservationId}
```

Main endpoints are `POST/GET /api/ships`, `GET /api/bornes`,
`GET /api/bornes/{borneId}`, `POST /api/bornes/{borneId}/reservations`, and
`GET /api/reservations/{id}` with `start`, `consumption`, `complete`, and
`cancel` subroutes. Invalid payloads return `400`, missing records return
`404`, and conflicts or invalid state transitions return `409`.

## Interactive simulator

Build and run:

```bash
java -jar borne-simulator/target/borne-simulator-0.0.1-SNAPSHOT.jar
```

The console asks for a positive number of Bornes (1-100 when interactive),
generates `B001`, `B002`, ... and assigns consecutive distinct ports starting
at `8081`. It does not ask for a utility type because every Borne provides
both electricity and water. Failed child startups are reported and already
started children are rolled back; option 3 or exit gracefully stops all
managed processes.

Launch parameters remain configurable:

```bash
java -jar borne-simulator/target/borne-simulator-0.0.1-SNAPSHOT.jar \
  --count=3 --base-port=8081 \
  --borne-id-prefix=B \
  --borne-jar=borne/target/borne-0.0.1-SNAPSHOT.jar
```

The simulator manages processes only. Use the REST workflow above for ship
registration and reservations.

## Tests

```bash
./mvnw test
./mvnw clean package
```

Tests cover dual-utility domain behavior, reservation lifecycle, progressive
pricing thresholds, simulator IDs/ports/startup rollback, and validation
logic.
