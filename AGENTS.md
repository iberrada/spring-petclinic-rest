# AGENTS.md - Spring PetClinic REST API

## Build & Run

- **Run locally**: `./mvnw spring-boot:run` (default H2 in-memory DB on port 9966)
- **CI build**: `./mvnw -B verify` (Maven build master, caches deps)
- **PR build**: `./mvnw -B verify` (same, no sonar)
- **Generate code** (DTOs + API interfaces): `mvn clean install` — runs OpenAPI generator
- **Generated code lives in**: `target/generated-sources/` (adds to classpath automatically)

## Test Execution

- **Unit/integration tests**: `./mvnw -B verify` — runs with H2 by default
- **Different DB profiles**: `spring.profiles.active=postgres` etc. (via `application.properties`)
- **Postman/Newman API tests**: `chmod +x postman-tests.sh && ./postman-tests.sh`
  - Requires: Node.js, jq, and API running at `http://localhost:9966`
  - Generates HTML reports in `src/test/postman/reports/`
- **JMeter performance tests**: `jmeter -n -t src/test/jmeter/petclinic-jmeter-crud-benchmark.jmx -Jthreads=100 -Jduration=600 -Jops=2000 -Jramp_time=120 -l results/petclinic-test-results.jtl`
  - API must be running first

## Database

- **Default**: H2 in-memory, auto-populated with sample data at startup
- **Switch DB**: Set `spring.profiles.active=<db>,spring-data-jpa` in `application.properties`
- **Supported**: H2 (default), HSQLDB, MySQL, PostgreSQL
- **H2 Console**: `http://localhost:9966/petclinic/h2-console` (JDBC: `jdbc:h2:mem:petclinic`, user: `sa`)

## Architecture Notes

- **Three repo layers**: `rest` (controllers v1/v2), `service` (ClinicServiceImpl), `repository` (JDBC, JPA, Spring Data JPA)
- **Generated code**: OpenAPI plugin runs at `generate-sources` phase; MapStruct processor runs at compile
- **Security**: Disabled by default. Enable via `petclinic.security.enable=true` in `application.properties`
- **API base URL**: `http://localhost:9966/petclinic/api` (OpenAPI serves at `/swagger-ui.html` and `/v3/api-docs`)

## CI / GitHub Actions

- **Master CI**: `.github/workflows/maven-build-master.yml` — builds + SonarQube
- **PR CI**: `.github/workflows/maven-build-pull-request.yml` — `./mvnw -B verify`
- **Newman smoke test**: `.github/workflows/newman-pipeline.yml` — starts API, runs `bash postman-tests.sh`, uploads HTML reports
- **Docker build**: `mvn compile jib:build -Djib.to.auth.username=xxx -Djib.to.auth.password=xxxxx`