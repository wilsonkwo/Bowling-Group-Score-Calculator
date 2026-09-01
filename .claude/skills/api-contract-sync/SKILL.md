---
name: api-contract-sync
description: Use whenever adding, removing, or modifying a REST API endpoint, request field, response field, validation rule, or HTTP status code. Enforces the project's mandatory three-way contract sync (openapi.yaml + Postman collection + okf/api docs) so no API change is left half-documented.
---

# API contract sync

This project treats the API contract as a first-class artifact. An API change is **not complete** until every contract file is updated in the same change.

## The mandatory sync rule

Any change to an endpoint, request field, response field, validation rule, or HTTP status code **must** update all three of these in the same commit:

1. `openapi.yaml` — OpenAPI 3.0 spec at the project root (paths, schemas, constraints, responses)
2. `bowling-api.postman_collection.json` — Postman collection at the project root (request body, URL, params, test scripts)
3. The matching `okf/api/*.md` — one doc per controller: `auth.md`, `bowlers.md`, `sessions.md`, `scores.md`

Also check `IFS_Bowling_Group_Score_Calculator_API.docx` (Interface Specification Document at the root) — if the changed endpoint is specified there, update it too.

## Procedure

1. Make the code change (controller, DTO, validation annotations).
2. Update controller annotations accurately — SpringDoc serves `/v3/api-docs.yaml` and `/swagger-ui.html` live from annotations, so they must match the hand-maintained `openapi.yaml`.
3. Edit `openapi.yaml`: path, method, parameters, request schema (field names, types, constraints like `@NotBlank` → `required`), response schema, and every possible status code (200/400/401/403/404...).
4. Edit `bowling-api.postman_collection.json`: request URL, method, headers, sample body, query params, and any test scripts that assert on the response shape.
5. Edit the matching `okf/api/*.md`: endpoint table, request/response examples, auth/role requirements (most writes need `ROLE_ADMIN`; all non-auth endpoints need `Authorization: Bearer <jwt>`).
6. Update the IFS docx if it covers this endpoint.
7. Add/extend contract coverage in `src/test/java/sg/sports/bowling/BowlingApiIntegrationTest.java` (MockMvc against H2, profile `test`) for the new/changed behavior.
8. Run `./mvnw test` (or `runTest.bat`) — do not consider the change complete until green.

## Remember the cross-cutting rules

- All endpoints except `/api/auth/login` and `/api/auth/register` require a Bearer JWT.
- Admin-only write operations require `ROLE_ADMIN` — document role requirements.
- `GlobalExceptionHandler` maps `IllegalArgumentException` → 400 with a message JSON — include that 400 in the contract where the service can throw it.
- CORS only allows `http://localhost:5173`; if an endpoint change implies a new frontend origin, that touches `SecurityConfig.corsConfigurationSource` (and `okf/tech_stack.md`).
