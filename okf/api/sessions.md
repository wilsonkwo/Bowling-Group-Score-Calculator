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
| POST | `/` | ROLE_ADMIN | `{ sessionDate, location?, notes? }` | created session (`status = OPEN`) |
| POST | `/{id}/close` | ROLE_ADMIN | — | session with `status = CLOSED` |
| GET | `/{id}/games` | any authenticated | — | list of [Game](../entities/game.md) for the session |
| POST | `/{id}/games` | ROLE_ADMIN | — | created Game |
