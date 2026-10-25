# Evidence Index

All runs on 25 Oct 2026 on the author's Windows 11 PC: Jenkins 2.x at `http://localhost:8081`, Docker Desktop 4.90 (Engine 29.7.2), JDK 17.0.20, Maven 3.10.0.

| Step | Deliverable | Evidence | Result |
|------|-------------|----------|--------|
| 1-3 | Problem definition, Agile plan, requirements & architecture | `docs/step-01-03/*.docx` | Done |
| 4 | Repository, README, issue templates, branch policy | GitHub repo, `.github/ISSUE_TEMPLATE/`, README "Branch Strategy" | Done |
| 5 | Feature branch + PR | PR #1 `feature/user-registration` | Done |
| 6 | Functional MVP, merge conflict, tag, backlog | PR #2, conflict fix `9e1822d`, tags `v1.0.0` `v1.1.0`, `docs/step-06-mvp-backlog.md` | Done |
| 7 | Jenkins CI job, trigger, archived artefact | `step-07/` - `Vehicle-Inspection-CI` build #10 "Started by an SCM change" (8 polling-triggered builds #3-#10), polling log, archived jar | SUCCESS |
| 8 | Jenkinsfile, pipeline run, deployed app, parameters | `step-08/jenkins-pipeline-build-13-console.log` - branch `ci/pipeline-deploy`, params `DEPLOY_ENV=dev APP_PORT=9090 NGINX_PORT=8088`; app healthy on 9090, Nginx on 8088 | SUCCESS |
| 9 | Test plan, Selenium scripts, local report, failure screenshots | `docs/step-09-test-plan.md`, `src/test/java/.../selenium/`, `step-09/local-selenium-run.log` (local `mvn -Pselenium test`, 25 Oct 2026 15:54: **Tests run: 6, Failures: 0, BUILD SUCCESS**) + per-class reports `step-09/*.txt`, failure screenshots in `step-10/` | SUCCESS |
| 10 | Jenkins test report, failed pipeline, fix commit, green rerun | `step-10/` - build #18 FAILED (J4/J5 caught the defect, deploy skipped) -> build #19 SUCCESS | Done |
| 11 | Dockerfile, image details, command log, running container | `Dockerfile`, `step-11/docker-lifecycle.log` (build -> tag -> inspect -> run 8090:8080 -> healthy -> logs -> stats -> stop -> start -> restart -> rm) | Done |
| 12 | Versioned image, registry, commit-to-container pipeline | `step-12/jenkins-pipeline-build-19-console.log`, `step-12/registry-tags.json` (`1.2.17`, `1.2.19`, `latest`) | SUCCESS |
| 13 | Config spec, Ansible playbook, first execution | `docs/step-13-configuration-management.md`, `ansible/`, `step-13-14/01-first-run.log` | SUCCESS |
| 14 | Provisioned node, idempotency, health check, rollback | `step-13-14/` - `02-idempotency-rerun.log` (**changed=0**), `03-health.log` (UP), `05-manual-rollback.log` (1.3.4-hotfix -> 1.3.4), `06-auto-rollback.log` (**ROLLBACK COMPLETE**), Jenkins `Vehicle-Inspection-Provisioning` build #4 | SUCCESS |

## Test results

| Suite | Build #13 (Step 8) | Build #18 (defect) | Build #19 (fixed) |
|-------|--------------------|--------------------|-------------------|
| API tests (`InspectionWorkflowApiTest`, 6) | 6 passed | 6 passed | 6 passed |
| Selenium J1 register + login | - | passed | passed |
| Selenium J1b wrong password | - | passed | passed |
| Selenium J2 submit request | - | passed | passed |
| Selenium J3 validation errors | - | passed | passed |
| Selenium J4 reviewer approves | - | **failed** | passed |
| Selenium J5 reject needs remarks | - | **failed** | passed |

Build #13 ran the single original Selenium test (`InspectionSeleniumTest`), which passed.

## Running services after the runs

| URL | What |
|-----|------|
| http://localhost:8088 | Nginx -> container `vi-app-dev` (image `localhost:5000/vehicle-inspection-workflow:1.2.19`) |
| http://localhost:8085 | `vi-app-dev` directly |
| http://localhost:5000/v2/_catalog | Local Docker registry |
| http://localhost:8095 | Ansible-provisioned node `vi-node1` (Nginx -> app release 1.3.4) |
