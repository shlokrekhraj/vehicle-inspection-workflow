# Step 10 - Continuous Testing in Jenkins

## How the pipeline gates deployment

```
Checkout -> Build & Unit Test -> Package -> Selenium Test -> Deploy
                 |                               |
         JUnit report (API tests)      JUnit report (browser tests)
                                       + failure screenshots archived
```

- `Build & Unit Test` runs `mvn clean test` (H2-backed API tests). A failure stops the pipeline here.
- `Selenium Test` starts the packaged jar on port 8080, waits for `/actuator/health`, then runs `mvn -Pselenium test`. Reports go to `target/selenium-reports/` and are published with the `junit` step.
- `Deploy` has a `when { currentBuild.currentResult == 'SUCCESS' }` gate. If any test failed, Jenkins shows the Deploy stage as **skipped** and the previously deployed version keeps running.
- On a Selenium failure the PNG and HTML of the failing page are archived from `target/selenium-screenshots/`.

## Deliberate defect exercise

| Build | Branch / commit | Result | What happened |
|-------|-----------------|--------|---------------|
| **#18** (25 Oct 2026, 15:34) | `demo/step10-defect` @ `0ebabc8` - *test: introduce deliberate defect in reviewer decision buttons* | **FAILURE** | Reviewer page sends `APPROVED`/`REJECTED` instead of `APPROVE`/`REJECT`. All 6 API tests and 4 of 6 Selenium tests passed; **J4 `reviewerApprovesRequest` and J5 `reviewerRejectsRequestWithRemarks` failed** with "Decision must be APPROVE or REJECT". `Publish Image` and `Deploy Container` were **skipped**, so nothing broken was released. Screenshots of both failing pages archived. |
| **#19** (25 Oct 2026, 15:37) | `develop` @ `243e5ae` (contains fix commit `69a8be7` - *fix: send APPROVE/REJECT decision from reviewer dashboard*) | **SUCCESS** | 12/12 tests passed; image `1.2.19` published and deployed. |

Evidence: `docs/evidence/step-10/` - failed build console log and the two failure screenshots captured automatically by `ScreenshotOnFailureExtension`. The green rerun is `docs/evidence/step-12/jenkins-pipeline-build-19-console.log`.

The registry holds `1.2.17` and `1.2.19` but **no `1.2.18`** - the defective build was never published.
