# Step 7 - Jenkins Installation and Continuous Integration Job

| Item | Value |
|------|-------|
| Jenkins | Windows service, `http://localhost:8081`, JDK `JDK-17`, Maven `Maven-3.10.0` tools configured |
| Job | `Vehicle-Inspection-CI` (freestyle) |
| Repository | `https://github.com/shlokrekhraj/vehicle-inspection-workflow.git`, branch `*/develop` |
| Build | Maven `clean package` |
| Trigger | **Poll SCM `H/5 * * * *`** - Jenkins checks GitHub every 5 minutes and builds when `develop` changes |
| Artefact | `target/vehicle-inspection-workflow-0.0.1-SNAPSHOT.jar` archived on every build |

## Trigger evidence

Builds #3 to #10 were all **"Started by an SCM change"** - each one followed a push to `develop` on 25 Oct 2026. See `docs/evidence/step-07/`:

- `jenkins-ci-build-10-console.log` - checkout, Maven build, `Archiving artifacts`, `Finished: SUCCESS`
- `jenkins-ci-build-10-scm-polling.log` - the polling run that detected the new commit
