---
type: JPA Entity
title: Role
description: Authorization role assigned to a User, e.g. ADMIN or USER.
resource: src/main/java/sg/sports/bowling/entity/Role.java
tags: [entities, auth]
timestamp: 2026-06-27T00:00:00Z
---
# Table: `role`

| Column | Type | Description |
|--------|------|--------------|
| `id` | BIGINT (PK) | Identity. |
| `name` | STRING, unique, not null | Role name, e.g. `ADMIN`, `USER`. |

# Used by
- [User](user.md) (many-to-many).
- `@PreAuthorize("hasRole('ADMIN')")` gates write operations in [API](../api/index.md).
