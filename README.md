## Docker container

### Build & Execution

Run the command `docker-compose build` and `docker-compose up` to build and start the containers respectively.
This container includes the Spring Boot backend, Angular frontend, MySql database and JSReport.


## Backend tests

Run `./mvnw test` from `back/`. The tests need no MySQL, SMTP or jsReport: `back/src/test/resources/application.properties` points them at an in-memory H2 database and sets `app.seed-demo-data=false` so the demo-data seeder in `BackApplication` does not run.
