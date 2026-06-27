---
type: API
title: Scores API
description: Submit and retrieve per-bowler frame scores, and fetch a session leaderboard.
resource: src/main/java/sg/sports/bowling/controller/ScoreController.java
tags: [api]
timestamp: 2026-06-27T00:00:00Z
---
# `/api/scores`

| Method | Path | Auth | Body / Params | Returns |
|--------|------|------|----------------|---------|
| POST | `/frames` | any authenticated | `{ bowlerId, gameId, frames: [[pins...], ...] }` | list of `FrameResponse` for that bowler+game |
| GET | `/frames` | any authenticated | `?bowlerId=&gameId=` | list of `FrameResponse` |
| GET | `/games/{gameId}/participants` | any authenticated | — | list of `ParticipantResponse { bowlerId, bowlerName, totalScore, gamePoints, result, frames }` — every bowler currently entered in the game, with frames so far |
| GET | `/leaderboard` | any authenticated | `?sessionId=` | list of `LeaderboardEntry { bowlerId, bowlerName, totalPoints }` |

`frames` is a list of per-frame ball arrays — `[pins]` for a strike, `[b1, b2]` normally, `[b1, b2, b3]` for frame 10. A submission may contain just the frames entered so far (1 to 10) — submitting always **replaces** all existing frames for that bowler+game with the given list, and triggers [scoring recalculation](../metrics/scoring.md) for every bowler in the game. This is how the frontend does frame-by-frame entry: each time a frame is completed for a bowler, it re-submits that bowler's full frame list so far. A `BowlerGame` row (and thus a "participant") is created implicitly on the first frame submission for a bowler+game — there is no separate "add participant" endpoint; `GET /games/{gameId}/participants` is how the frontend discovers who has already been entered when resuming a game.
