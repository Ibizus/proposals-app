# PropFlow

PropFlow (shown as "Proposal Admin" in the UI) is a demo application for managing job proposals: candidates, positions, offices, proposals, salary breakdowns with configurable taxes, PDF proposal letters and candidate emails.

## Stack

- `back/` — Java 21 / Spring Boot 3.2 REST API, MySQL 8, Hibernate, springdoc OpenAPI and in-JVM PDF generation with OpenHTMLtoPDF.
- `front/` — Angular 17 standalone single-page app with PrimeNG, Tailwind CSS and ngx-translate (English, Spanish, Portuguese and German).
- Mailpit for local email capture.
- Docker Compose runs the frontend, backend, MySQL and Mailpit; `./dev.sh` runs the backend and frontend locally against containerized MySQL and Mailpit.

## Prerequisites

- Docker with the Compose plugin (`docker compose`), used by `./dev.sh` and the Docker demo.
- For local development outside Docker: JDK 21, Node.js 20 and npm.
- Google Chrome to run the frontend tests.

## Docker demo

Build and start everything from the repository root:

```bash
docker compose build
docker compose up
```

| Service          | URL / port                                                        |
| ---------------- | ----------------------------------------------------------------- |
| Frontend         | <http://localhost:4200>                                           |
| Backend API      | <http://localhost:8080/v1/api>                                    |
| Swagger UI       | <http://localhost:8080/swagger-ui.html>                           |
| Mailpit web UI   | <http://localhost:8025>                                           |
| Mailpit SMTP     | `localhost:1025`                                                  |
| MySQL            | `localhost:3306` (database `proposals_db`, user `root`, password `user`) |

The backend container runs the `dev` Spring profile, so it seeds demo data into an empty database (see [Demo data](#demo-data)). The login form accepts any non-empty username and password; there is no real authentication (see [Known limitations](#known-limitations)).

Stop the stack with `docker compose down`, or `docker compose down --volumes` to also delete the MySQL data.

`docker-compose.yml` uses fixed `container_name`s while the Compose project name comes from the checkout folder name, so a second checkout in a differently named folder can collide with an existing stack. Remove the stale container with `docker rm -f <name>` and start again.

## Local development

Run `./dev.sh` from the repository root. It starts MySQL and Mailpit with Docker Compose, waits until the `mysql` healthcheck reports healthy (up to 120 seconds, override with `MYSQL_WAIT_TIMEOUT`), then starts the backend with the `dev` Spring profile and the Angular dev server. Other secrets are read from a `.env` file in the repository root when present.

- `./dev.sh --reset` removes the project's Docker containers and the MySQL volume before starting, so the backend comes up on an empty database and seeds fresh demo data.
- `./dev.sh --help` prints the available options.

The backend listens on <http://localhost:8080>, the Angular dev server on <http://localhost:4200> and Mailpit on <http://localhost:8025>. The frontend API base URL is set in `front/src/environments/`.

Copy `.env.example` to `.env` only when you need to override the defaults; `./dev.sh` sources it and `.env` is gitignored.

## Tests and checks

The CI workflow (`.github/workflows/ci.yml`) runs the backend checks and tests, the frontend format/lint/test/build steps and a `docker compose build` smoke test on pushes and pull requests to `main`.

Backend, from `back/` (no MySQL or SMTP needed):

```bash
./mvnw test
./mvnw spotless:check
./mvnw checkstyle:check
./mvnw package -DskipTests
```

Tests use an in-memory H2 database configured in `back/src/test/resources/application.properties`. Spotless only checks files changed since its ratchet ref (`spotless.ratchetFrom` in `back/pom.xml`); Checkstyle checks the whole backend.

Frontend, from `front/` (Chrome required):

```bash
npm ci
npm test              # Karma/Jasmine in a visible Chrome, watch mode
npm run test:ci       # headless Chrome with coverage, as CI runs it
npm run lint
npm run format:check
npm run build
```

## Mail

Email is sent through Mailpit by default, the zero-secret local SMTP server that `./dev.sh` and `docker-compose.yml` start: the backend connects to `localhost:1025` (no username, no password, no `.env`), and captured messages are browsable at <http://localhost:8025>. The mail health indicator is disabled by default (`management.health.mail.enabled=false`), so a missing or misconfigured local SMTP server never takes `/actuator/health` DOWN.

To send real mail, override the Spring Mail settings through environment variables — for example Resend:

```bash
SPRING_MAIL_HOST=smtp.resend.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=resend
SPRING_MAIL_PASSWORD=<resend API key>
SPRING_MAIL_SMTP_AUTH=true
APP_MAIL_FROM=noreply@<your verified Resend domain>
```

Real keys belong only in the gitignored `.env` file (see `.env.example`) or in deployment environment variables.

## Demo data

`DemoDataSeeder` (`back/src/main/java/org/propflow/back/seed/`) fills an empty database with fictitious demo data: 5 positions, 24 candidates spread across them, and 16 proposals (only for the candidates that reached the final stage), each with their office, salary, extras and status histories, plus the extra templates, the Spain (active by default) and Portugal tax configurations (Portugal's IRS brackets come from `back/src/main/resources/seed/taxes.csv`) and the salary parameters the app needs. The data is generated with Datafaker from a fixed seed, so every fresh database gets the same rows.

- The switch is the `app.seed-demo-data` property. It defaults to `false`; the `dev` profile (`application-dev.properties`, activated by `./dev.sh`) sets it to `true`. The `backend` service in `docker-compose.yml` also activates the `dev` profile, so `docker compose up` seeds the same data.
- The seeder only fills tables that are still empty, so restarting the app never duplicates rows. Use `./dev.sh --reset` to start over.

## API documentation and observability

The backend exposes:

- **Swagger UI** at `/swagger-ui.html` (which redirects to `/swagger-ui/index.html`), a short `/openapi` redirect to it, and the OpenAPI document at `/v3/api-docs`. API info, controller tags, operation summaries and schema descriptions are provided by springdoc-openapi.
- **Actuator** endpoints under `/actuator`: `health`, `info`, `metrics` and `prometheus` are exposed; the sensitive endpoints stay closed.
- **Structured JSON logs**: activate the `prod` Spring profile (`SPRING_PROFILES_ACTIVE=prod`) to switch the console output from readable logs to structured JSON via the Logstash encoder. The default and `dev` profiles keep readable console logs.

## Documentation

- `CONTEXT.md` — domain glossary for the terms used in the code and UI.
- `docs/adr/` — architecture decision records.
- `back/README.md` and `front/README.md` — area-specific notes.
- `.env.example` — environment variables that can be overridden.

## Known limitations

This is a demo, and deployment concerns are deliberately unfinished:

- The login form accepts any non-empty username and password, and the backend `SecurityConfig` permits all requests (it carries a TODO for real security).
- The frontend API base URL is hard-coded to `http://localhost:8080/v1/api/` in `front/src/environments/`, and the REST controllers allow cross-origin requests from `localhost:4200` and `localhost:3000` only.
- The schema is created and updated by Hibernate (`spring.jpa.hibernate.ddl-auto=update`); there are no database migrations.
