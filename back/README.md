# proposals-app backend

Java 21 / Spring Boot backend for the Proposal Admin demo.

## Requirements

- JDK 21+
- Maven
- Docker

## Local development

Use the root `dev.sh` script, which starts MySQL 8 and Mailpit via `docker-compose.yml` and then the Spring Boot application:

```bash
./dev.sh
```

To reset the database and reseed the demo data:

```bash
./dev.sh --reset
```

Demo seeding is controlled by `app.seed-demo-data=true` (set in `application.properties` and enabled by default in the local profile). Seeding is idempotent and covered by `DemoDataSeederTest`.

## Stack

- Java 21
- Spring Boot 3
- MySQL 8
- Mailpit for local email capture
