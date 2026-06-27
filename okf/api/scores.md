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
| GET | `/leaderboard` | any authenticated | `?sessionId=` | list of `LeaderboardEntry { bowlerId, name, points }` |

`frames` is a list of per-frame ball arrays — `[pins]` for a strike, `[b1, b2]` normally, `[b1, b2, b3]` for frame 10. Submitting replaces all existing frames for that bowler+game and triggers [scoring recalculation](../metrics/scoring.md) for every bowler in the game.
