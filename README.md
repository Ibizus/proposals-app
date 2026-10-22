## Docker container

### Build & Execution

Run the command `docker-compose build` and `docker-compose up` to build and start the containers respectively.
This container includes the Spring Boot backend, Angular frontend, MySql database and JSReport.


## Local development

Run `./dev.sh` from the repository root. It starts MySQL, Mailpit and jsReport with Docker Compose, waits until the `mysql` healthcheck reports healthy (up to 120 seconds, override with `MYSQL_WAIT_TIMEOUT`), then starts the backend with the `dev` Spring profile and the Angular dev server. Other secrets are read from a `.env` file in the repository root when present.

- `./dev.sh --reset` removes the project's Docker containers and the MySQL volume before starting, so the backend comes up on an empty database and seeds fresh demo data.
- `./dev.sh --help` prints the available options.

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

### Demo data

`DemoDataSeeder` (`back/src/main/java/org/propflow/back/seed/`) fills an empty database with fictitious demo data: 30 proposals with their candidates, offices, salaries, extras and status histories, plus the extra templates, tax brackets (`back/src/main/resources/seed/taxes.csv`) and salary parameters the app needs. The data is generated with Datafaker from a fixed seed, so every fresh database gets the same rows.

- The switch is the `app.seed-demo-data` property. It defaults to `false`; the `dev` profile (`application-dev.properties`, activated by `./dev.sh`) sets it to `true`. The `backend` service in `docker-compose.yml` also activates the `dev` profile, so `docker-compose up` seeds the same data.
- The seeder only fills tables that are still empty, so restarting the app never duplicates rows. Use `./dev.sh --reset` to start over.

## Backend tests

Run `./mvnw test` from `back/`. The tests need no MySQL, SMTP or jsReport: `back/src/test/resources/application.properties` points them at an in-memory H2 database and sets `app.seed-demo-data=false` and `app.reset-pdf-folder=false` so no startup runner touches data or files.

## API documentation and observability

The backend exposes:

- **Swagger UI** at `/swagger-ui.html` (which redirects to `/swagger-ui/index.html`) and the OpenAPI document at `/v3/api-docs`. API info, controller tags, operation summaries and schema descriptions are provided by springdoc-openapi.
- **Actuator** endpoints under `/actuator`: `health`, `info`, `metrics` and `prometheus` are exposed; the sensitive endpoints stay closed.
- **Structured JSON logs**: activate the `prod` Spring profile (`SPRING_PROFILES_ACTIVE=prod`) to switch the console output from readable logs to structured JSON via the Logstash encoder. The default and `dev` profiles keep readable console logs.
