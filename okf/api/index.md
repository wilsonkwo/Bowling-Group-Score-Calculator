---
type: Index
title: API
description: REST endpoints exposed by the bowling backend, grouped by controller.
tags: [api]
timestamp: 2026-06-27T00:00:00Z
---
# API
- [Auth](auth.md) — `/api/auth/*` login, register, refresh, current user
- [Bowlers](bowlers.md) — `/api/bowlers/*` CRUD
- [Sessions](sessions.md) — `/api/sessions/*` sessions and their games
- [Scores](scores.md) — `/api/scores/*` frame entry and leaderboard

All endpoints except `/api/auth/*` require a `Authorization: Bearer <jwt>` header (see [JwtAuthFilter](../../src/main/java/sg/sports/bowling/security/JwtAuthFilter.java)). Write operations on bowlers and sessions require `ROLE_ADMIN`.

# Error responses
A `400 Bad Request` with `{ "message": "<reason>" }` is returned for invalid input or a not-found/duplicate lookup (e.g. "Bowler not found: 5", "Bowler already exists: Alice") — see [GlobalExceptionHandler](../../src/main/java/sg/sports/bowling/config/GlobalExceptionHandler.java), which translates any uncaught `IllegalArgumentException` from the service layer. This exists because Spring Boot's default behavior — forwarding uncaught exceptions to `/error` — collided with this app's security config: that internal forward has no `Authorization` header, so without the handler (and `/error` being permitted in `SecurityConfig`), every such error was masked as a misleading `401 Full authentication is required` instead of its real status.
