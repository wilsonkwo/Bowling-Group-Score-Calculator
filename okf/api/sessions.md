---
type: API
title: Sessions API
description: Manage BowlingSession records and the Games within them.
resource: src/main/java/sg/sports/bowling/controller/SessionController.java
tags: [api]
timestamp: 2026-06-27T00:00:00Z
---
# `/api/sessions`

| Method | Path | Auth | Body / Params | Returns |
|--------|------|------|----------------|---------|
| GET | `/` | any authenticated | — | list of [BowlingSession](../entities/session.md) |
| GET | `/open` | any authenticated | — | sessions with `status = OPEN` |
| GET | `/{id}` | any authenticated | — | a session |
| POST | `/` | ROLE_ADMIN | `{ sessionDate, timeSlot, location?, notes? }` | created session (`status = OPEN`) |
| POST | `/{id}/close` | ROLE_ADMIN | — | session with `status = CLOSED` |
| DELETE | `/{id}` | ROLE_ADMIN | — | `204 No Content`; cascades to games, bowler_games, frames |
| GET | `/{id}/games` | any authenticated | — | list of [Game](../entities/game.md) for the session |
| POST | `/{id}/games` | ROLE_ADMIN | — | created Game |

`timeSlot` must be one of `MORNING`, `AFTERNOON`, `EVENING` ([BowlingSession.TimeSlot](../entities/session.md)) — missing or invalid value returns `400`. The combination of `sessionDate` + `timeSlot` must be unique; a duplicate returns `400 { "message": "A session already exists for <date> (<slot>)" }`.
