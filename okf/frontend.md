---
type: System
title: Frontend
description: React + Vite + TypeScript single-page app that consumes the bowling REST API.
resource: frontend/
tags: [frontend, react, vite]
timestamp: 2026-06-27T00:00:00Z
---
# Overview
A separate Vite/React/TypeScript project under `frontend/` (its own `package.json`, not part of the Maven build). It calls the existing REST API directly — see [API](api/index.md) — there is no server-side rendering or backend-for-frontend layer. UI components come from [Mantine](tech_stack.md) (`@mantine/core`, `@mantine/hooks`, `@mantine/notifications`) rather than hand-rolled CSS.

# Structure
- `frontend/src/api/` — `client.ts` (axios instance, attaches `Authorization: Bearer <jwt>` from `localStorage`, clears it on 401), `auth.ts` (login/register/`changePassword`), `bowlers.ts`
- `frontend/src/auth/AuthContext.tsx` — login/logout, persists `token`/`username`/`roles` in `localStorage`, exposes `isAdmin`
- `frontend/src/components/` — `ProtectedRoute` (redirects to `/login` if unauthenticated), `Layout` (Mantine `AppShell` with header + collapsible navbar, `Menu` for the user dropdown: Change password, Log out)
- `frontend/src/pages/` — `LoginPage` (Mantine `Paper`/`TextInput`/`PasswordInput`), `BowlersPage` (Mantine `Table` with inline edit via `ActionIcon`, admin-gated per [Bowlers API](api/bowlers.md) role rules), `ChangePasswordPage` (Mantine form, success reported via `@mantine/notifications`, calls [`/api/auth/change-password`](api/auth.md)), `SessionsPage` (list/create/close [sessions](entities/session.md)), `GameScorePage` (pick a session and game, multi-select bowlers from the bowlers list, enter frame-by-frame scores in a shared scoresheet table so all selected bowlers progress through frames together — see `frontend/src/utils/frameRules.ts` for ball-input validation and [Scores API](api/scores.md) for how frame submission works)
- `frontend/src/utils/frameRules.ts` — pure helpers for 10th-frame bonus-ball rules and building the per-bowler ball arrays to submit
- `frontend/postcss.config.cjs` — required by Mantine: `postcss-preset-mantine` + `postcss-simple-vars` for breakpoint variables
- `frontend/src/main.tsx` — wraps the app in `MantineProvider` + `Notifications`, imports `@mantine/core/styles.css` and `@mantine/notifications/styles.css` before the app's own `index.css`

# Running it
```
cd frontend
npm install
npm run dev      # http://localhost:5173
```
Requires the backend running on `http://localhost:8080` (see [Tech Stack](tech_stack.md) for the CORS note — only `http://localhost:5173` is allowed by `SecurityConfig`).

# Status
Login, change password (via the top-nav user menu), the Bowlers page (full CRUD), Session List (create/close sessions), and Add Game Score (pick session + game, select bowlers, frame-by-frame entry) are implemented end-to-end. A standalone Leaderboard page is not yet built — `GET /api/scores/leaderboard` is available but nothing in the UI calls it yet.
