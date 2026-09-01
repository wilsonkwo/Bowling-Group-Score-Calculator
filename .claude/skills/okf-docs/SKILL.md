---
name: okf-docs
description: Use whenever a change touches anything documented in okf/ (entities, endpoints, scoring rules, frontend structure, tech stack), or when adding new entities/features that need documenting. Covers the OKF documentation conventions, the same-change update rule, and HTML regeneration.
---

# OKF documentation maintenance

This project documents its domain model, API, and scoring rules in `okf/` using Google's Open Knowledge Format. It is the source of truth so a human or AI agent can load accurate context without re-reading every source file. **Stale docs are a defect** — update them in the same change as the code.

## Layout

- `okf/index.md` — system overview; start here; links everything else
- `okf/entities/*.md` — JPA entities (User, Role, Bowler, BowlingSession, Game, BowlerGame, Frame)
- `okf/api/*.md` — REST endpoints per controller (auth, bowlers, sessions, scores)
- `okf/metrics/scoring.md` — frame scoring and group win/loss point calculation
- `okf/frontend.md` — frontend structure and how to run it
- `okf/tech_stack.md` — languages, frameworks, libraries, DB, with exact versions
- `okf/architecture.svg` — canonical architecture diagram (embedded in `okf/index.md`)

## File conventions

- YAML frontmatter on every file: `type`, `title`, `description`, `resource` (path to the source file it documents), `tags`, `timestamp`.
- Cross-link with relative markdown links (e.g. `[Frame](../entities/frame.md)`).
- New entity → new `okf/entities/<name>.md` plus a link from `okf/index.md`.
- New endpoint group → update the matching `okf/api/*.md` (and see the `api-contract-sync` skill for the other mandatory contract files).
- Frontend pages added → update the "Status" section of `okf/frontend.md`.
- Version bumps (Java, Spring Boot, driver, React, Vite...) → update `okf/tech_stack.md` with exact versions.

## HTML regeneration

The `.html` files beside each `.md` are **generated output, not source** — never hand-edit them. After editing any `okf/*.md`, regenerate the browsable HTML copies:

```bat
okf\generate_docs.bat
```

or:

```powershell
okf/generate_docs.ps1
```
