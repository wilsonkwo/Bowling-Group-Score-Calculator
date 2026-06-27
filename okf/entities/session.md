---
type: JPA Entity
title: BowlingSession
description: A date/location grouping of one or more games (a single outing).
resource: src/main/java/sg/sports/bowling/entity/BowlingSession.java
tags: [entities]
timestamp: 2026-06-27T00:00:00Z
---
# Table: `bowling_session`

| Column | Type | Description |
|--------|------|--------------|
| `id` | BIGINT (PK) | Identity. |
| `session_date` | DATE, not null | Date of the outing. |
| `location` | STRING | Optional venue. |
| `notes` | STRING | Optional free text. |
| `status` | STRING enum, not null | `OPEN` (default) or `CLOSED`. |

# Relationships
- One-to-many with [Game](game.md).

# API
Managed via [`/api/sessions`](../api/sessions.md).
