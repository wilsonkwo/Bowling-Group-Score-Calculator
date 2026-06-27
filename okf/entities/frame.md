---
type: JPA Entity
title: Frame
description: One of the 10 frames bowled within a BowlerGame, with raw ball pin counts and computed scores.
resource: src/main/java/sg/sports/bowling/entity/Frame.java
tags: [entities]
timestamp: 2026-06-27T00:00:00Z
---
# Table: `frame`
Unique on (`bowler_game_id`, `frame_number`).

| Column | Type | Description |
|--------|------|--------------|
| `id` | BIGINT (PK) | Identity. |
| `bowler_game_id` | BIGINT (FK, not null) | The [BowlerGame](bowler_game.md). |
| `frame_number` | INT, not null | 1–10. |
| `ball1` | INT | Pins on 1st ball. |
| `ball2` | INT | Pins on 2nd ball (null if strike in frames 1–9). |
| `ball3` | INT | Bonus ball, only used in frame 10. |
| `frame_score` | INT | This frame's contribution, including strike/spare lookahead bonus. |
| `cumulative_score` | INT | Running total through this frame. |
| `frame_points` | DOUBLE | Bonus points for this frame: `1` for a spare, `2` for a strike, `0` otherwise. |

# Helper methods
- `isStrike()` — `ball1 == 10`.
- `isSpare()` — not a strike and `ball1 + ball2 == 10`.

# Computed by
[Scoring algorithm](../metrics/scoring.md).
