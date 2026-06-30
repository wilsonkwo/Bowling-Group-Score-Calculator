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

A bowler with any existing [BowlerGame](../entities/bowler_game.md) record cannot be deleted — `BowlerService.deleteBowler` checks for this explicitly and returns `400` (`"Cannot delete {name}: they have existing game history"`) rather than relying on the DB's foreign key constraint, which doesn't reliably fail the same way across H2 (tests) and MariaDB (prod).
