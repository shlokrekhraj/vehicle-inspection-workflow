# Step 11 - Docker Image and Container Lifecycle

## Image

| Item | Value |
|------|-------|
| Dockerfile | `Dockerfile` (repo root) |
| Base image | `eclipse-temurin:17-jre-alpine` (JRE only, small) |
| Image name | `vehicle-inspection-workflow:<version>` + `:latest` |
| User | non-root `vehicleapp` |
| Port | 8080 (`EXPOSE 8080`) |
| Health check | `HEALTHCHECK` calls `/actuator/health` every 15 s |
| Labels | OCI title, version, git revision, author |

Configuration is passed with environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `APP_PORT`, `APP_ENV`, `JAVA_OPTS`.

## Lifecycle

`docker\container-lifecycle.ps1` runs and logs each stage:

build -> tag -> images / inspect / history -> network -> run (port map 8090:8080) -> ps / port / health -> logs -> stats -> stop -> start -> restart -> rm

The log is written to `docs/evidence/docker-lifecycle.log`.

## Full stack with Compose

`docker compose up -d --build` starts MySQL, the app and Nginx (`http://localhost:8088`). `docker compose down` stops everything; add `-v` to also delete the database volume.
