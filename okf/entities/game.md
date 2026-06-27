---
type: JPA Entity
title: Game
description: A single numbered game within a BowlingSession, carrying the win-point value for that game.
resource: src/main/java/sg/sports/bowling/entity/Game.java
tags: [entities]
timestamp: 2026-06-27T00:00:00Z
---
# Table: `game`
Unique on (`session_id`, `game_number`).

| Column | Type | Description |
|--------|------|--------------|
| `id` | BIGINT (PK) | Identity. |
| `session_id` | BIGINT (FK, not null) | Parent [BowlingSession](session.md). |
| `game_number` | INT, not null | 1, 2, 3... within the session. |

# Relationships
- Many-to-one with [BowlingSession](session.md).
- One-to-many with [BowlerGame](bowler_game.md).

# See also
[Scoring](../metrics/scoring.md) — the game winner bonus is a fixed 3 points, not stored per-game.
