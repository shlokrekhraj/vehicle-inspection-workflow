# Step 9 - Selenium Test Plan

## Scope

End-to-end browser tests of the critical user journeys of the Vehicle Inspection Workflow, run with Selenium WebDriver (Chrome, headless by default) and JUnit 5 through Maven.

| ID | Journey | Test class / method | Test data | Expected result |
|----|---------|---------------------|-----------|-----------------|
| J1 | Applicant registers and logs in | `UserAccountSeleniumTest.registerThenLogin` | unique `applicant<timestamp>@test.com`, password `secret123` | "Registered ... as APPLICANT", then "Welcome, Selenium Applicant" |
| J1b | Wrong password rejected | `UserAccountSeleniumTest.wrongPasswordIsRejected` | unknown email | "Invalid email or password" (HTTP 401) |
| J2 | Submit a valid inspection request | `SubmitRequestSeleniumTest.validRequestIsSubmitted` | random valid number e.g. `MH12AB4821`, Car, Registration Certificate | "Request ID n for MH.. is SUBMITTED" |
| J3 | Validation of bad input | `SubmitRequestSeleniumTest.invalidRequestShowsValidationErrors` | `12345`, no type, owner `Al`, no document | Error list incl. vehicle number format, type and document required |
| J4 | Reviewer approves, applicant tracks | `ReviewWorkflowSeleniumTest.reviewerApprovesRequest` | new request + remark "All documents verified" | Dashboard shows APPROVED; status page shows APPROVED + remark |
| J5 | Rejection requires remarks | `ReviewWorkflowSeleniumTest.reviewerRejectsRequestWithRemarks` | new request; reject without remarks, then with "Pollution certificate expired" | First attempt refused; then REJECTED + remark on status page |

## Assertions

Every journey asserts on visible page text (message boxes, status badges) and on success/error styling, not just on the absence of exceptions.

## Failure screenshots

`ScreenshotOnFailureExtension` (JUnit 5 `AfterTestExecutionCallback`) runs before the browser closes. On any failure it saves:

- `target/selenium-screenshots/<Class>_<method>_<timestamp>.png`
- `target/selenium-screenshots/<Class>_<method>_<timestamp>.html` (page source)

Jenkins archives this folder.

## Running locally

```powershell
# 1. Start the app (needs MySQL running; DB is created automatically)
mvn spring-boot:run

# 2. In a second terminal, run the browser suite
mvn -Pselenium test -Dapp.baseUrl=http://localhost:8080

# Watch the browser instead of headless mode
mvn -Pselenium test -Dselenium.headless=false
```

Reports: `target/surefire-reports/` (XML + TXT). `mvn test` without the profile runs only the fast API tests.

## Environment

Chrome (Selenium Manager downloads the matching ChromeDriver automatically), JDK 17, Maven, MySQL.

## Local execution result

Run on the author's PC on 25 Oct 2026 at 15:54 against the deployed app (`http://localhost:8088`):

```
mvn -B -Pselenium test -Dapp.baseUrl=http://localhost:8088
Tests run: 2, Failures: 0 -- ReviewWorkflowSeleniumTest
Tests run: 2, Failures: 0 -- SubmitRequestSeleniumTest
Tests run: 2, Failures: 0 -- UserAccountSeleniumTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Full log: `docs/evidence/step-09/local-selenium-run.log`. Per-class reports: `docs/evidence/step-09/*.txt`.
The failure-screenshot mechanism was exercised in Jenkins build #18 (see `docs/evidence/step-10/`).
