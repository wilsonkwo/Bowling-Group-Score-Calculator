---
type: System
title: Tech Stack
description: Languages, frameworks, libraries, and tools used by this project, with versions.
resource: pom.xml
tags: [tech-stack, versions]
timestamp: 2026-06-27T00:00:00Z
---
# Backend
| Component | Version | Notes |
|-----------|---------|-------|
| Java | 21 | `pom.xml` `java.version` |
| Spring Boot | 3.3.5 | parent POM; pulls in Spring Framework 6.1.14 |
| Spring Web (`spring-boot-starter-web`) | 3.3.5 | REST controllers |
| Spring Data JPA (`spring-boot-starter-data-jpa`) | 3.3.5 | repositories/entities, Hibernate ORM 6.5.3 |
| Spring Security (`spring-boot-starter-security`) | 3.3.5 | auth, method security; Spring Security 6.3.4 |
| Spring Boot Validation (`spring-boot-starter-validation`) | 3.3.5 | `@Valid` request DTOs; field errors returned as `{ "message": "Validation failed", "errors": { "<field>": "<message>" } }` |
| SpringDoc OpenAPI (`springdoc-openapi-starter-webmvc-ui`) | 2.6.0 | Auto-generates OpenAPI 3 spec from annotations; Swagger UI at `/swagger-ui.html`, raw YAML/JSON at `/v3/api-docs.yaml` and `/v3/api-docs` |
| Spring Boot DevTools | 3.3.5 | local dev auto-restart (runtime only) |
| JJWT (`jjwt-api`/`jjwt-impl`/`jjwt-jackson`) | 0.12.6 | JWT issuing/verification, see [JwtUtil](../src/main/java/sg/sports/bowling/security/JwtUtil.java) |
| Lombok | 1.18.34 | `@Data`/`@Builder` boilerplate on entities/DTOs |

# Frontend
| Component | Version | Notes |
|-----------|---------|-------|
| Node.js | v24.18.0 (dev machine) | required to run/build the frontend |
| React | 19.2.7 | UI library, `frontend/` |
| TypeScript | ~6.0.2 | strict-mode app code |
| Vite | 8.1.0 | dev server (`localhost:5173`) and build tool |
| react-router-dom | 7.18.0 | client-side routing (`/login`, `/bowlers`, protected layout) |
| axios | 1.18.1 | API client (`frontend/src/api/client.ts`), attaches `Authorization: Bearer <jwt>` from `localStorage` |
| @mantine/core, @mantine/hooks, @mantine/notifications | 9.4.0 | UI component library (AppShell, forms, tables, notifications) — see [frontend.md](frontend.md) |
| @tabler/icons-react | 3.44.0 | icon set used alongside Mantine components |
| postcss, postcss-preset-mantine, postcss-simple-vars | 8.5.15 / 1.18.0 / 7.0.1 | PostCSS plugins required by Mantine's stylesheet (`frontend/postcss.config.cjs`) |

The frontend is a separate project under `frontend/`, calling the backend's REST API directly (see [API](api/index.md)). The backend allows CORS from `http://localhost:5173` only (`SecurityConfig.corsConfigurationSource`) — update that origin list if the frontend's dev port or deployed origin changes.

# Database
| Component | Version | Notes |
|-----------|---------|-------|
| MariaDB | server: operator-provided; driver `mariadb-java-client` 3.3.3 | production/dev datasource, configured in `application.properties` |
| H2 | 2.2.224 | in-memory DB used only for the `test` Spring profile (`application-test.properties`) — see [scoring.md](metrics/scoring.md) and [BowlingApiIntegrationTest](../src/test/java/sg/sports/bowling/BowlingApiIntegrationTest.java) |

# Testing
| Component | Version | Notes |
|-----------|---------|-------|
| JUnit Jupiter | 5.10.5 | via `spring-boot-starter-test` |
| Mockito | 5.11.0 | service-level unit tests (`ScoreServiceTest`) |
| AssertJ | 3.25.3 | fluent assertions |
| Spring Test / MockMvc | 6.1.14 | endpoint integration tests |
| Spring Security Test | 6.3.4 | security-aware test support |

# Documentation
| Component | Notes |
|-----------|-------|
| Google Open Knowledge Format (OKF) | This `okf/` directory — markdown files with YAML frontmatter, cross-linked, documenting entities, API, and scoring rules. See [index.md](index.md). |

# Build & tooling
- Maven (via `mvnw`/`mvnw.cmd` wrapper) — build, test, run.
- `run.bat` — starts the app locally against MariaDB.
- `runTest.bat` — runs the test suite (`mvnw test`) against the in-memory H2 `test` profile, no external DB needed.
