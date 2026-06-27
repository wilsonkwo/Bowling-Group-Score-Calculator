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
