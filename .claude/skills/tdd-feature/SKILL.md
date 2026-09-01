---
name: tdd-feature
description: Use for any new feature or bug fix that touches application logic (services, scoring rules, anything with a business rule). Enforces the project's test-driven red-green-refactor workflow and its test placement conventions.
---

# TDD feature workflow

This project follows test-driven development for all application logic. Do not write implementation before the failing test exists.

## Red — Green — Refactor

1. **Red** — write a failing unit test that expresses the desired behavior *before* writing the implementation.
2. **Green** — write the minimum code to make it pass.
3. **Refactor** — clean up with tests still passing.

## Where tests go

- **Service / business logic**: unit tests under `src/test/java/sg/sports/bowling/service/` using Mockito-mocked repositories — no database needed. Follow the pattern in `ScoreServiceTest`.
- **Endpoint / contract behavior** (auth, role checks, request/response shape, full flows): add to `BowlingApiIntegrationTest` using MockMvc against the in-memory H2 database. Config: `src/test/resources/application-test.properties`, profile `test`. No MariaDB instance or environment variables are required to run tests.
- **Scoring rule changes** (frame points, win bonus, completion gating): the change is not done until tests exist that pin down the new numbers — assert exact expected values, not ranges.

## Rules

- Never skip the red phase: watch the new test fail first, for the right reason.
- Existing tests must stay green throughout; a refactor that breaks a test is a regression, not a refactor.
- Keep test style consistent with the existing suite (JUnit 5, Mockito, AssertJ, Spring Security Test where auth is involved).

## Definition of done

Run the full suite before considering a change complete:

```bash
./mvnw test
```

or on Windows:

```bat
runTest.bat
```

If the change touches an API endpoint, the `api-contract-sync` skill also applies (openapi.yaml + Postman + okf/api docs in the same change). If it touches documented domain behavior, the `okf-docs` skill applies too.
