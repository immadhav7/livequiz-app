# LiveQuiz App

A small quiz web application: a teacher creates a quiz and gets a join code, students join, submit answers and see a leaderboard.

The app is intentionally simple. **The main project is the delivery platform that builds, ships and runs it:**
https://github.com/immadhav7/livequiz-deploy

That repo covers the GitOps setup: Terraform-created EC2 server running k3s, a Helm chart, and ArgoCD deploying to dev and prod.

## What this repo contains

- Spring Boot 4.1.1, Java 21, Gradle wrapper, MySQL
- Multi-stage `Dockerfile` (non-root user, healthcheck)
- `docker-compose.yml` for running the app with MySQL locally
- `.github/workflows/ci.yml`, the CI pipeline

## API

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/quizzes` | Create a quiz and get a join code |
| GET | `/api/quizzes/{code}` | Fetch a quiz by join code |
| POST | `/api/quizzes/{code}/submissions` | Submit answers |
| GET | `/api/quizzes/{code}/leaderboard` | View the leaderboard |

Health endpoints (Actuator) expose `liveness` and `readiness` groups, used by the Kubernetes probes.

## Run locally

```
docker compose up --build
```

The app is then available at `http://localhost:8080`, for example `http://localhost:8080/actuator/health`.

Database settings come from the environment variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` and `DB_PASSWORD`, with defaults for local use.

## Tests

```
./gradlew build
```

Runs 7 API tests (MockMvc with in-memory H2) plus the application context test.

## CI pipeline

Every push to `main` runs three jobs:

1. **build-test:** `./gradlew build`, uploads the test report.
2. **image:** builds the Docker image and pushes it to GHCR, tagged with the first 7 characters of the commit SHA.
3. **update-deploy-repo:** writes the new tag into `values-dev.yaml` in the deploy repo using a deploy key limited to that repo. ArgoCD then rolls out dev.

Image: `ghcr.io/immadhav7/livequiz-app:<7-char-commit-sha>`

## Note

The application code was written with AI assistance. The delivery platform is the focus of the project.
