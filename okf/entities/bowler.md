---
type: JPA Entity
title: Bowler
description: A player who participates in games, optionally linked to a User login account.
resource: src/main/java/sg/sports/bowling/entity/Bowler.java
tags: [entities]
timestamp: 2026-06-27T00:00:00Z
---
# Table: `bowler`

| Column | Type | Description |
|--------|------|--------------|
| `id` | BIGINT (PK) | Identity. |
| `name` | STRING, not null | Display name. |
| `user_id` | BIGINT (FK, nullable) | Optional link to [User](user.md) for self-service login. |

# Relationships
- One-to-many with [BowlerGame](bowler_game.md) (a bowler's results across games).
