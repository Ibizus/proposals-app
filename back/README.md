# SpringBoot App with Dockerized PostgresSQL

## Requirements
- JDK 17+
- Maven
- Docker
- Postgres Docker Official Image

## Connecting to Dockerized PostgresSQL

1. Run the `docker run` comand for the postgres image
  ```
docker run --env=POSTGRES_USER=postgres --env=POSTGRES_PASSWORD=postgres --env=POSTGRES_DB=db --env=PGDATA=/var/lib/postgresql/data -p 5432:5432 -d postgres:latest
  ```

2. Configure Spring Boot Application
   Update the application.properties file to specify the Postgres database connection details
   (host, port, username, password, database name)

3. Run Spring Boot Application inside vscode
  
