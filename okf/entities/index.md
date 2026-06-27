---
type: Index
title: Entities
description: JPA entities for the bowling domain model.
tags: [entities, jpa]
timestamp: 2026-06-27T00:00:00Z
---
# Entities
- [User](user.md) — login account
- [Role](role.md) — authorization role (ADMIN, USER)
- [Bowler](bowler.md) — a player, optionally linked to a User
- [BowlingSession](session.md) — a date/location grouping of games
- [Game](game.md) — one game within a session
- [BowlerGame](bowler_game.md) — a bowler's participation/result in a game
- [Frame](frame.md) — one of the 10 frames within a BowlerGame

# Relationships
```
BowlingSession 1──* Game 1──* BowlerGame *──1 Bowler ──1 User
                                  │
                                  └──* Frame
```
