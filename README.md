## Docker container

### Build & Execution

Run the command `docker-compose build` and `docker-compose up` to build and start the containers respectively.
This container includes the Spring Boot backend, Angular frontend, MySql database and JSReport.


## Local development

Run `./dev.sh` from the repository root. It starts MySQL and jsReport with Docker Compose, waits until the `mysql` healthcheck reports healthy (up to 120 seconds, override with `MYSQL_WAIT_TIMEOUT`), then starts the backend with the `dev` Spring profile and the Angular dev server. Mail settings and other secrets are read from a `.env` file in the repository root when present.

- `./dev.sh --reset` removes the project's Docker containers and the MySQL volume before starting, so the backend comes up on an empty database and seeds fresh demo data.
- `./dev.sh --help` prints the available options.

### Demo data

`DemoDataSeeder` (`back/src/main/java/org/propflow/back/seed/`) fills an empty database with fictitious demo data: 30 proposals with their candidates, offices, salaries, extras and status histories, plus the extra templates, tax brackets (`back/src/main/resources/seed/taxes.csv`) and salary parameters the app needs. The data is generated with Datafaker from a fixed seed, so every fresh database gets the same rows.

- The switch is the `app.seed-demo-data` property. It defaults to `false`; the `dev` profile (`application-dev.properties`, activated by `./dev.sh`) sets it to `true`. The `backend` service in `docker-compose.yml` also activates the `dev` profile, so `docker-compose up` seeds the same data.
- The seeder only fills tables that are still empty, so restarting the app never duplicates rows. Use `./dev.sh --reset` to start over.

## Backend tests

Run `./mvnw test` from `back/`. The tests need no MySQL, SMTP or jsReport: `back/src/test/resources/application.properties` points them at an in-memory H2 database and sets `app.seed-demo-data=false` and `app.reset-pdf-folder=false` so no startup runner touches data or files.
