---
type: System
title: Frontend
description: React + Vite + TypeScript single-page app that consumes the bowling REST API.
resource: frontend/
tags: [frontend, react, vite]
timestamp: 2026-06-27T00:00:00Z
---
# Overview
A separate Vite/React/TypeScript project under `frontend/` (its own `package.json`, not part of the Maven build). It calls the existing REST API directly — see [API](api/index.md) — there is no server-side rendering or backend-for-frontend layer.

# Structure
- `frontend/src/api/` — `client.ts` (axios instance, attaches `Authorization: Bearer <jwt>` from `localStorage`, clears it on 401), `auth.ts` (login/register/`changePassword`), `bowlers.ts`
- `frontend/src/auth/AuthContext.tsx` — login/logout, persists `token`/`username`/`roles` in `localStorage`, exposes `isAdmin`
- `frontend/src/components/` — `ProtectedRoute` (redirects to `/login` if unauthenticated), `Layout` (nav + user dropdown menu: Change password, Log out)
- `frontend/src/pages/` — `LoginPage`, `BowlersPage` (list/create/edit/delete, admin-gated per [Bowlers API](api/bowlers.md) role rules), `ChangePasswordPage` (calls [`/api/auth/change-password`](api/auth.md))

# Running it
```
cd frontend
npm install
npm run dev      # http://localhost:5173
```
Requires the backend running on `http://localhost:8080` (see [Tech Stack](tech_stack.md) for the CORS note — only `http://localhost:5173` is allowed by `SecurityConfig`).

# Status
Login, change password (via the top-nav user menu), and the Bowlers page (full CRUD) are implemented end-to-end. Sessions/Games/Scores/Leaderboard pages are not yet built — extend following the same `api/` + `pages/` pattern.
