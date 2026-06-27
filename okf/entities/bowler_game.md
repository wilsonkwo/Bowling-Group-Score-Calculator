---
type: JPA Entity
title: BowlerGame
description: A bowler's participation and result in a single Game — total score, win/loss/draw, and points earned.
resource: src/main/java/sg/sports/bowling/entity/BowlerGame.java
tags: [entities]
timestamp: 2026-06-27T00:00:00Z
---
# Table: `bowler_game`
Unique on (`bowler_id`, `game_id`).

| Column | Type | Description |
|--------|------|--------------|
| `id` | BIGINT (PK) | Identity. |
| `bowler_id` | BIGINT (FK, not null) | The [Bowler](bowler.md). |
| `game_id` | BIGINT (FK, not null) | The [Game](game.md). |
| `total_score` | INT | Sum of frame scores; recomputed whenever frames are saved. |
| `result` | STRING enum | `WIN`, `LOSS`, or `DRAW`; set after all bowlers' frames are in. |
| `game_points` | DOUBLE | Sum of this bowler's frame points (spares/strikes) plus a 3-point bonus if they won the game (split evenly among ties). |

# Relationships
- Many-to-one with [Bowler](bowler.md) and [Game](game.md).
- One-to-many with [Frame](frame.md).

# Computed by
[ScoreService.saveFrames / recalculateResults](../metrics/scoring.md).
