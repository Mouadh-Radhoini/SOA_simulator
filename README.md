# Port ship reservation simulator

This project is a software simulator for utility stations in a maritime port.
A ship registers with the REST service, reserves an available Borne, and
simulates electricity and water service. No physical pumps, cables, batteries,
sensors, or other equipment are controlled.

Every Borne provides **both electricity and water**. The two utilities have
independent consumption measurements and progressive tariffs.

## Architecture

The repository contains two Maven applications:

- `borne/` is one Spring Boot REST API server. It manages all Borne IDs through
  one HTTP port and persists shared state in PostgreSQL.
- `borne-simulator/` is one Java console application. It creates one
  `BorneSimulationTask` per Borne and runs those tasks concurrently in a
  bounded `ExecutorService`.

The simulator does **not** start a Spring Boot application, HTTP server, or
operating-system process per Borne. It does not use `ProcessBuilder`, does not
assign a port to each Borne, and never connects directly to PostgreSQL.
Communication is:

```text
BorneSimulationTask -> BorneApiClient -> REST API
    -> application services -> domain rules -> JPA/Flyway -> PostgreSQL
```

The API retains the layered structure of web controllers/DTOs, application
use cases, domain entities and transitions, and persistence infrastructure.

## Domain and lifecycle

The database contains `bornes`, `ships`, and `reservations`. A reservation
references one ship and one Borne. Electricity in kWh, water in liters, and
final electricity/water/total costs are stored on the reservation so billing
cannot mix ships' usage.

The reservation workflow is:

1. Register a ship.
2. Reserve an available Borne.
3. Start the reservation.
4. Record `electricityKwh` and `waterLiters`.
5. Complete the reservation and persist final charges.
6. Release the Borne to `AVAILABLE`.

Reservations may be cancelled while `RESERVED`. Bornes use
`AVAILABLE -> RESERVED -> IN_USE -> AVAILABLE` and also support
`OUT_OF_SERVICE`. Pessimistic row locking and PostgreSQL partial unique indexes
prevent concurrent active reservations from claiming the same Borne or ship.

## Pricing

Configured defaults are:

| Utility | Tier 1 | Tier 2 | Tier 3 |
|---|---:|---:|---:|
| Electricity | first 10 kWh at 0.30 DT/kWh | next to 20 kWh at 0.45 | above 20 at 0.60 |
| Water | first 100 L at 0.002 DT/L | next to 500 L at 0.003 | above 500 at 0.005 |

Pricing is progressive marginal pricing implemented with `BigDecimal`. For
example, 14 kWh costs `4.80 DT`, 600 liters costs `1.90 DT`, and the total is
the sum of both utility costs. Rates and thresholds are configurable through
`application.yaml` environment variables.

## Build and database

Requirements: JDK 21 and the Maven wrapper. Docker Compose exposes PostgreSQL
on host port `5433`:

```bash
docker compose up -d postgres
./mvnw clean package
```

Database variables are `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. The Borne
API variables include `BORNE_ID`, `BORNE_POWER_KW`, `BORNE_WATER_FLOW_LPM`, and
`SERVER_PORT`. Flyway preserves V1/V2 history and applies
`V3__add_ships_reservations_and_dual_utility_usage.sql`; existing migrations
are not rewritten.

## REST API example

Start one API server:

```bash
java -jar borne/target/borne-0.0.1-SNAPSHOT.jar \
  --server.port=8080 --borne.id=bootstrap \
  --borne.power-kw=7.2 --borne.water-flow-lpm=12
```

Register a ship and reserve a Borne:

```bash
curl -X POST http://localhost:8080/api/ships \
  -H 'Content-Type: application/json' \
  -d '{"id":"S001","name":"Atlantic Star"}'

curl -X POST http://localhost:8080/api/bornes/B001/reservations \
  -H 'Content-Type: application/json' \
  -d '{"shipId":"S001"}'
```

Use the returned reservation UUID:

```bash
curl -X POST http://localhost:8080/api/reservations/{reservationId}/start

curl -X POST http://localhost:8080/api/reservations/{reservationId}/consumption \
  -H 'Content-Type: application/json' \
  -d '{"electricityKwh":14,"waterLiters":600}'

curl -X POST http://localhost:8080/api/reservations/{reservationId}/complete
curl http://localhost:8080/api/reservations/{reservationId}
```

The main routes are:

- `POST/GET /api/ships`
- `GET /api/bornes` and `GET /api/bornes/{borneId}`
- `POST /api/bornes/{borneId}/reservations`
- `GET /api/reservations/{id}`
- `POST /api/reservations/{id}/start`
- `POST /api/reservations/{id}/consumption`
- `POST /api/reservations/{id}/complete`
- `POST /api/reservations/{id}/cancel`

Invalid payloads return `400`, missing records return `404`, and conflicts or
invalid lifecycle transitions return `409`.

## Thread-based simulator

Start the API once, then run the simulator as a separate JVM:

```bash
java -jar borne-simulator/target/borne-simulator-0.0.1-SNAPSHOT.jar \
  --api-url=http://localhost:8080 \
  --count=3 \
  --max-workers=3 \
  --interval-ms=1000 \
  --request-timeout-ms=2000 \
  --max-retries=2
```

With `--count` omitted, the console asks for a number from 1 to 100. It
generates `B001`, `B002`, and so on, ensures each Borne exists through the REST
API, and starts one task per ID. `--max-workers` bounds the number of tasks
actively executing; queued tasks remain tracked and are stopped during
shutdown. The default worker policy is deliberately non-destructive: workers
register/query their assigned Borne and report status, but do not invent ships,
reservations, or random consumption. Use the documented REST workflow to
exercise reservations and billing.

The console commands are:

```text
status
stop
exit
```

`status` reports `CREATED`, `RUNNING`, `FAILED`, or `STOPPED` per Borne.
`stop`/`exit`, end-of-input, and the JVM shutdown hook interrupt workers,
await executor termination for a bounded period, and report if termination is
not clean. API availability is checked before tasks are submitted. GET
requests use bounded retries; non-idempotent POST registration is not blindly
retried.

## Tests

Run all tests and package artifacts with:

```bash
./mvnw clean test
./mvnw clean package
```

Tests cover domain lifecycle/pricing, persistence-facing reservation behavior,
simulator input validation, unique IDs, bounded concurrent task execution,
HTTP API interaction, task state tracking, retry boundaries, and graceful
shutdown.
