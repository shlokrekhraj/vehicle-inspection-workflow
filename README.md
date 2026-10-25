# Containerized Vehicle Inspection Workflow

A web-based Vehicle Inspection Workflow developed as an academic Software Engineering and DevOps project.

## Project Overview

The system provides a centralized workflow for submitting, validating, reviewing and tracking vehicle inspection requests.

Main workflow:

Applicant → Submit Request → Validate → Reviewer Review → Approve/Reject → Status Tracking

## Objectives

- Allow applicants to submit vehicle inspection requests.
- Validate vehicle and document information.
- Allow reviewers to examine submitted requests.
- Support approval and rejection.
- Provide inspection status tracking.
- Implement Git and GitHub based development.
- Automate builds using Jenkins.
- Automate browser testing using Selenium.
- Containerize the application using Docker.
- Automate configuration using Ansible.

## MVP

1. User Registration
2. User Login
3. Vehicle Details Entry
4. Inspection Request Creation
5. Document Information Submission
6. Input Validation
7. Request Storage
8. Reviewer Dashboard
9. Request Details View
10. Review Action
11. Approval
12. Rejection
13. Status Tracking
14. Review Remarks
15. Basic Error Handling

## Technology Stack

- Java
- Spring Boot
- Maven
- MySQL
- HTML
- CSS
- JavaScript
- Bootstrap
- Git
- GitHub
- Jenkins
- Selenium
- Docker
- Ansible
- Nginx

## Branch Strategy

- main
- develop
- feature/<feature-name>
- bugfix/<bug-name>
- hotfix/<issue-name>
- release/<version>

## Commit Convention

Examples:

- feat: add user registration
- feat: add inspection request
- fix: correct request validation
- test: add inspection test
- docs: update README
- build: configure Maven
- ci: add Jenkins pipeline

## DevOps Lifecycle

PLAN → CODE → BUILD → TEST → RELEASE → DEPLOY → OPERATE → MONITOR → FEEDBACK

## Student

Name: Shlok Rekhraj

Roll Number: 23102C0045

Institute: Vidyalankar Institute of Technology

Department: Computer Engineering

## Running the Application

```powershell
# Requires JDK 17, Maven and MySQL (the database is created automatically)
$env:DB_PASSWORD = "<mysql root password>"
mvn spring-boot:run
# http://localhost:8080  - account, submit request, reviewer dashboard, status tracking
```

| Command | What it does |
|---------|--------------|
| `mvn test` | Fast API tests on an in-memory H2 database |
| `mvn -Pselenium test -Dapp.baseUrl=http://localhost:8080` | Selenium browser suite against a running app |
| `docker compose up -d --build` | MySQL + app + Nginx on http://localhost:8088 |
| `ansible\run-ansible.ps1 -Playbook site.yml` | Provision, configure and deploy with Ansible |

## API

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/users/register` | Register applicant or reviewer |
| POST | `/api/users/login` | Login |
| POST | `/api/inspection-requests` | Submit request (validated) |
| GET | `/api/inspection-requests?status=` | Reviewer dashboard |
| GET | `/api/inspection-requests/{id}` | Request details |
| PUT | `/api/inspection-requests/{id}/review` | Start review |
| PUT | `/api/inspection-requests/{id}/decision?decision=APPROVE\|REJECT&remarks=` | Approve / reject |
| GET | `/api/inspection-requests/{id}/status` | Status tracking |
| GET | `/actuator/health` | Health check |

## DevOps Implementation

| Step | Deliverable | Location |
|------|-------------|----------|
| 6 | MVP completion and updated backlog | `docs/step-06-mvp-backlog.md` |
| 7 | Jenkins CI job (`Vehicle-Inspection-CI`, SCM polling, archived jar) | `docs/step-07-jenkins-ci.md` |
| 8 | Pipeline as code with Nginx deploy | `Jenkinsfile`, `deploy/`, `docs/step-08-pipeline-as-code.md` |
| 9 | Selenium test plan and suite | `docs/step-09-test-plan.md`, `src/test/java/.../selenium` |
| 10 | Continuous testing and deploy gate | `docs/step-10-continuous-testing.md` |
| 11 | Docker image and lifecycle | `Dockerfile`, `docker/`, `docs/step-11-docker.md` |
| 12 | Jenkins-Docker continuous deployment | `Jenkinsfile`, `docs/step-12-continuous-deployment.md` |
| 13 | Ansible configuration management | `ansible/`, `docs/step-13-configuration-management.md` |
| 14 | Provisioning, idempotency, health, rollback | `Jenkinsfile.provision`, `docs/step-14-provisioning-reliability.md` |

All run logs, reports and screenshots: [`docs/evidence/README.md`](docs/evidence/README.md)

