---
type: JPA Entity
title: User
description: Login account, used for authentication and optionally linked to a Bowler.
resource: src/main/java/sg/sports/bowling/entity/User.java
tags: [entities, auth]
timestamp: 2026-06-27T00:00:00Z
---
# Table: `users`

| Column | Type | Description |
|--------|------|--------------|
| `id` | BIGINT (PK) | Identity. |
| `username` | STRING, unique, not null | Login name. |
| `password` | STRING, not null | BCrypt hash. |
| `email` | STRING | Optional contact email. |
| `enabled` | BOOLEAN, not null | Defaults `true`; disables login when `false`. |

# Relationships
- Many-to-many with [Role](role.md) via join table `user_roles`.
- Optionally one-to-one with [Bowler](bowler.md) (`Bowler.user`).

# Gotcha: `roles` must be a mutable Set
When constructing a `User` (e.g. in `UserService.registerUser`, `DataInitializer`), always assign `roles` a mutable collection (`new HashSet<>(...)`), never `Set.of(...)`. Hibernate wraps whatever collection instance is passed at first save as the managed `PersistentSet`; an immutable `Set.of(...)` there causes `UnsupportedOperationException` the next time that user entity is `save()`'d (e.g. in [`UserService.changePassword`](../api/auth.md)), because merge tries to `clear()` it.
