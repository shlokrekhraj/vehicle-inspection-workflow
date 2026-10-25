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

| Build | Commit | What happened |
|-------|--------|---------------|
| _fill in_ | `test: introduce deliberate defect in reviewer approve button` | Reviewer page sends `decision=APPROVED` instead of `APPROVE`. API tests still pass; Selenium J4 fails; Deploy skipped; screenshot archived. |
| _fill in_ | `fix: send APPROVE decision from reviewer dashboard` | Defect corrected; all tests pass; Deploy runs. |

Evidence: Jenkins test result trend, the failed build's console + screenshot artifact, and the green rerun.
