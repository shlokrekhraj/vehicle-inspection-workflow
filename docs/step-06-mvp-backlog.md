# Step 6 - MVP Completion: Updated Backlog

| # | MVP task | Status | Where it lives |
|---|----------|--------|----------------|
| 1 | User Registration | Done | `POST /api/users/register`, `index.html` |
| 2 | User Login | Done | `POST /api/users/login` (BCrypt-hashed passwords), `index.html` |
| 3 | Vehicle Details Entry | Done | `apply.html`, `InspectionRequest` |
| 4 | Inspection Request Creation | Done | `POST /api/inspection-requests` |
| 5 | Document Information Submission | Done | `documentName` field |
| 6 | Input Validation | Done | `InspectionValidationService` (vehicle number format, vehicle type, required fields) |
| 7 | Request Storage | Done | MySQL via Spring Data JPA |
| 8 | Reviewer Dashboard | Done | `GET /api/inspection-requests?status=`, `reviewer.html` |
| 9 | Request Details View | Done | `GET /api/inspection-requests/{id}` |
| 10 | Review Action | Done | `PUT /api/inspection-requests/{id}/review` |
| 11 | Approval | Done | `PUT /{id}/decision?decision=APPROVE` |
| 12 | Rejection | Done | `PUT /{id}/decision?decision=REJECT&remarks=...` (remarks mandatory) |
| 13 | Status Tracking | Done | `GET /{id}/status`, `status.html` |
| 14 | Review Remarks | Done | `remarks` column, shown on dashboard and status page |
| 15 | Basic Error Handling | Done | `GlobalExceptionHandler` - consistent JSON for 400/401/404/409 |

## Workflow states

`SUBMITTED -> UNDER_REVIEW -> APPROVED | REJECTED`

Actions out of order (for example approving a request that is still `SUBMITTED`) return **409 Conflict**.

## Git collaboration evidence

- Feature branches: `feature/user-registration` (PR #1), `feature/inspection-workflow` (PR #2), `feature/mvp-completion`
- Merge conflict: README conflict resolved in commit `9e1822d`
- Tags: `v1.0.0` (first MVP baseline), `v1.1.0` (complete MVP)

## API tests

`mvn test` runs `InspectionWorkflowApiTest` against an in-memory H2 database (no MySQL needed): registration, login, validation, approve flow, reject-with-remarks flow, 404 handling and the health endpoint.
