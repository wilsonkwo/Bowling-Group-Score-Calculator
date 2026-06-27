---
type: API
title: Auth API
description: Login, registration, token refresh, and current-user endpoints.
resource: src/main/java/sg/sports/bowling/controller/AuthController.java
tags: [api, auth]
timestamp: 2026-06-27T00:00:00Z
---
# `/api/auth`

| Method | Path | Auth | Body / Params | Returns |
|--------|------|------|----------------|---------|
| POST | `/login` | none | `{ username, password }` | `{ token, type: "Bearer", username, roles }` |
| POST | `/register` | none | `{ username, email, password }` | `{ message }` |
| POST | `/refresh` | Bearer token | — | fresh `{ token, type, username, roles }` |
| GET | `/me` | Bearer token | — | `{ username, roles }` |
| POST | `/change-password` | Bearer token | `{ currentPassword, newPassword }` (newPassword min 8 chars) | `200 { message }`; `400 { message: "Current password is incorrect" }` if `currentPassword` doesn't match |

JWTs are issued/verified by [JwtUtil](../../src/main/java/sg/sports/bowling/security/JwtUtil.java).
