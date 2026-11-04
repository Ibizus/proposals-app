# proposals-app backend

Java 21 / Spring Boot 3.2 backend for the PropFlow (Proposal Admin) demo. It exposes the REST API under `/v1/api`, generates proposal PDFs in-process and sends proposal emails.

## Requirements

- JDK 21+
- Docker, for MySQL 8 and Mailpit via the root `docker-compose.yml`
- Maven is not required: the Maven wrapper (`./mvnw`) is included.

## Local development

Use the root `dev.sh` script, which starts MySQL 8 and Mailpit via `docker-compose.yml` and then the Spring Boot application:

```bash
./dev.sh
```

To reset the database and reseed the demo data:

```bash
./dev.sh --reset
```

Demo seeding is controlled by `app.seed-demo-data`: it is off by default in `application.properties` and enabled by the `dev` profile (`application-dev.properties`) that `dev.sh` starts the backend with. Seeding is idempotent and covered by `DemoDataSeederTest`.

The API listens on <http://localhost:8080>. While it runs, Swagger UI is at <http://localhost:8080/swagger-ui.html> and the OpenAPI document at <http://localhost:8080/v3/api-docs>.

## Tests and checks

From `back/` (tests use in-memory H2 and need no MySQL or SMTP):

```bash
./mvnw test
./mvnw spotless:check
./mvnw checkstyle:check
./mvnw package -DskipTests
```

Spotless only checks files changed since its ratchet ref (`spotless.ratchetFrom` in `pom.xml`). See the root `README.md` for the full-stack commands and demo setup.

## Stack

- Java 21
- Spring Boot 3.2
- MySQL 8
- Mailpit for local email capture
