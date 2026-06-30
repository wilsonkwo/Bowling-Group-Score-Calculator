# Bowling Group Score Calculator

> **Before doing anything else in this repo:** read this entire file, then read every file in `okf/` (start at [okf/index.md](okf/index.md) and follow its links — entities, API, scoring, frontend, tech stack). These are the source of truth for this project's domain model, API, business rules, and stack. Skipping them means working from stale assumptions.

Spring Boot 3 / Java 21 REST API for tracking bowling sessions, frame-by-frame scoring, and group leaderboards, with a React + Vite + TypeScript frontend under `frontend/`.

## Documentation: OKF

This project documents its domain model, API, and scoring rules in `okf/` using Google's Open Knowledge Format (OKF) — a directory of markdown files with YAML frontmatter (`type`, `title`, `description`, `resource`, `tags`, `timestamp`), cross-linked via relative markdown links. It exists so a human or an AI agent can quickly load accurate context on entities, endpoints, and business rules without re-reading every source file.

- `okf/index.md` — start here for the system overview
- `okf/entities/*.md` — JPA entities (User, Role, Bowler, BowlingSession, Game, BowlerGame, Frame)
- `okf/api/*.md` — REST endpoints per controller (auth, bowlers, sessions, scores)
- `okf/metrics/scoring.md` — frame scoring and group win/loss point calculation
- `okf/frontend.md` — frontend structure (React + Vite + TypeScript, `frontend/src/api`, `auth`, `components`, `pages`) and how to run it
- `okf/tech_stack.md` — languages, frameworks, libraries, and DB used, with exact versions (Java 21, Spring Boot 3.3.5, MariaDB driver 3.3.3, H2 2.2.224 for tests, JJWT 0.12.6, React 19.2.7, Vite 8.1.0)

**Read `okf/index.md` (and the relevant linked pages) before making changes to entities, endpoints, or scoring logic.** Whenever a change touches something documented there, update the matching `okf/*.md` file in the same change — do not leave the docs stale.

## API contract files — mandatory sync

Any change that adds, removes, or modifies an API endpoint, request field, response field, validation rule, or HTTP status code **must** update all three of these files in the same change:

1. `openapi.yaml` — OpenAPI 3.0 spec at the project root (paths, schemas, constraints, responses)
2. `bowling-api.postman_collection.json` — Postman collection at the project root (request body, URL, params, test scripts)
3. The matching `okf/api/*.md` — OKF API doc for the affected controller

Do not consider an API change complete until all three are updated.

The `.md` files are the source of truth; `okf/architecture.svg` is the canonical architecture diagram (also embedded in `okf/index.md`). Run `okf\generate_docs.bat` (or `okf/generate_docs.ps1`) to regenerate a browsable HTML copy of every `okf/*.md` file as a sibling `.html` (e.g. `okf/index.html`, `okf/entities/bowler.html`) — these `.html` files are generated output, not source; re-run the script after editing any `.md` file rather than hand-editing the HTML.

## Development process: TDD

This project follows test-driven development for application logic (services, scoring rules, anything with a business rule). Workflow for any new feature or bug fix:

1. **Red** — write a failing unit test that expresses the desired behavior before writing the implementation.
2. **Green** — write the minimum code to make it pass.
3. **Refactor** — clean up with tests still passing.

Guidelines:
- Unit-test services (`src/test/java/.../service/`) with Mockito-mocked repositories — no database needed. See `ScoreServiceTest` for the pattern.
- Endpoint/contract behavior (auth, role checks, request/response shape, full flows) is covered by `BowlingApiIntegrationTest` using `MockMvc` against an in-memory H2 database (`src/test/resources/application-test.properties`, profile `test`) — no MariaDB or env vars required to run tests. Add new endpoints there.
- A change to scoring rules (frame points, win bonus, etc.) is not done until tests exist that pin down the new numbers.
- Run `./mvnw test` (or `runTest.bat` on Windows) before considering a change complete.

## Frontend

`frontend/` is a separate Vite + React + TypeScript project (its own `package.json`, not part of the Maven build) that calls the backend's REST API directly. See [okf/frontend.md](okf/frontend.md) for structure and [okf/tech_stack.md](okf/tech_stack.md) for versions.

- Run with `cd frontend && npm install && npm run dev` (serves on `http://localhost:5173`). Requires the backend running on `http://localhost:8080`.
- The backend only allows CORS from `http://localhost:5173` (`SecurityConfig.corsConfigurationSource`) — update that origin list if the frontend's dev port or deployed origin ever changes, and update `okf/tech_stack.md` to match.
- Login/JWT state lives in `localStorage` (`token`, `username`, `roles`) via `frontend/src/auth/AuthContext.tsx`; `frontend/src/api/client.ts` attaches the bearer token to every request and clears storage on a 401.
- Only Login and the Bowlers page (full CRUD) exist so far. Add new pages following the same `api/` + `pages/` pattern, and update `okf/frontend.md`'s "Status" section as pages are added.
