---
type: API
title: Bowlers API
description: CRUD for Bowler records.
resource: src/main/java/sg/sports/bowling/controller/BowlerController.java
tags: [api]
timestamp: 2026-06-27T00:00:00Z
---
# `/api/bowlers`

| Method | Path | Auth | Body / Params | Returns |
|--------|------|------|----------------|---------|
| GET | `/` | any authenticated | — | list of [Bowler](../entities/bowler.md) |
| GET | `/{id}` | any authenticated | — | a Bowler |
| POST | `/` | ROLE_ADMIN | `{ name }` | created Bowler |
| PUT | `/{id}` | ROLE_ADMIN | `{ name }` | updated Bowler |
| DELETE | `/{id}` | ROLE_ADMIN | — | 204 No Content |
