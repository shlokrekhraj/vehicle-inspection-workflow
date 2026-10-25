# Step 8 - Pipeline as Code and Server Deployment

Pipeline job `Vehicle-Inspection-Pipeline` reads `Jenkinsfile` from the repository (Pipeline script from SCM).

## Stages (branch `ci/pipeline-deploy`)

`Checkout -> Build & Unit Test -> Package -> Selenium Test -> Deploy`

| Stage | What it does |
|-------|-------------|
| Checkout | `checkout scm`, prints Java / Maven / Docker versions |
| Build & Unit Test | `mvn clean test` + JUnit report |
| Package | `mvn package`, archives the jar |
| Selenium Test | starts the jar on 8080, waits for `/actuator/health`, runs the browser test |
| Deploy | copies the jar to `C:\vehicle-inspection-deploy\<env>\releases\`, starts it on `APP_PORT`, starts **Nginx** (`vi-nginx-<env>`) as reverse proxy on `NGINX_PORT`, health-checks through Nginx |

## Parameters (environment settings)

| Parameter | Default | Effect |
|-----------|---------|--------|
| `DEPLOY_ENV` | `dev` / `staging` | deploy folder, `APP_ENV` shown in `/actuator/info`, Nginx container name |
| `APP_PORT` | `9090` | port of the deployed app |
| `NGINX_PORT` | `8088` | public port |

## Evidence

Build **#13** - SUCCESS, 25 Oct 2026 15:09, commit `c1e35d7`, parameters `DEPLOY_ENV=dev APP_PORT=9090 NGINX_PORT=8088`: `docs/evidence/step-08/jenkins-pipeline-build-13-console.log` (ends with `Health check passed: http://localhost:8088/actuator/health -> UP`).

From Step 12 onward the same Jenkinsfile on `develop` deploys a Docker container instead of the jar; Nginx stays the entry point on port 8088.
