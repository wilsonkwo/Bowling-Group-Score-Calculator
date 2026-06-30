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
- `frontend/src/api/` — `client.ts` (axios instance, attaches `Authorization: Bearer <jwt>` from `localStorage`, clears it on 401), `auth.ts` (login/register/`changePassword`), `bowlers.ts`, `sessions.ts` (also exports the shared `TIME_SLOT_LABELS` map and `getSession(id)`)
- `frontend/src/auth/AuthContext.tsx` — login/logout, persists `token`/`username`/`roles` in `localStorage`, exposes `isAdmin`
- `frontend/src/components/` — `ProtectedRoute` (redirects to `/login` if unauthenticated), `AdminRoute` (redirects to `/` if the user isn't `ROLE_ADMIN` — gates the Data Management routes below), `Layout` (Mantine `AppShell` with header + collapsible navbar, `Menu` for the user dropdown: Change password, Log out)

## Navigation
The navbar (`Layout.tsx`) has two sections:
- **General** (all authenticated users): "Sessions" → `/` (`SessionsOverviewPage`), a read-only, descending-by-date list of [sessions](entities/session.md) with Open/Closed/All filter chips. Clicking a row navigates to `/sessions/:id` (`SessionDetailPage`), which shows that session's details plus a per-game breakdown of bowlers (total score, points, result) sourced from [`getGames`](api/sessions.md) + [`getParticipants`](api/scores.md).
- **Data Management** (admin only, hidden from the navbar and route-guarded by `AdminRoute` for non-admins): "Add / Modify Bowler Details" → `/manage/bowlers` (`BowlersPage`, full CRUD), "Add / Modify Session Details" → `/manage/sessions` (`SessionsPage`, create/close sessions), "Add / Modify Game Details" → `/manage/games` (`GameScorePage`, frame-by-frame score entry). The old `/bowlers`, `/sessions`, `/game-score` paths redirect to their `/manage/*` equivalents for old bookmarks/links.

# Pages
- `LoginPage` (Mantine `Paper`/`TextInput`/`PasswordInput`), `ChangePasswordPage` (Mantine form, success reported via `@mantine/notifications`, calls [`/api/auth/change-password`](api/auth.md))
- `SessionsOverviewPage` / `SessionDetailPage` — see Navigation above
- `BowlersPage` (Mantine `Table` with inline edit via `ActionIcon`; delete is blocked server-side with a `400` + message if the bowler has existing game history, shown via `@mantine/notifications` — see [Bowlers API](api/bowlers.md))
- `SessionsPage` (list/create/close [sessions](entities/session.md) — creating one requires picking a time slot via a `Select`, Open/Closed/All filter chips above the table)
- `GameScorePage` (pick a session and game, multi-select bowlers from the bowlers list, enter frame-by-frame scores in a shared scoresheet table so all selected bowlers progress through frames together — see `frontend/src/utils/frameRules.ts` for ball-input validation and [Scores API](api/scores.md) for how frame submission works)
- `frontend/src/utils/frameRules.ts` — pure helpers for 10th-frame bonus-ball rules and building the per-bowler ball arrays to submit
- `frontend/src/components/PinRack.tsx` + `BallCell.tsx` — clickable pin-deck input for each ball in `GameScorePage`'s scoresheet, replacing manual number entry. A ball cell shows the standard 1-2-3-4 pin triangle; **pins default to all "down"** (a strike) rather than all standing, since experienced bowlers clear most pins most throws — clicking a pin marks it as the one(s) still standing. The committed ball value is `pins shown - pins marked standing`, computed on popover close (`BallCell`'s `onCommit`)
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
Login, change password (via the top-nav user menu), the General session browser/detail view (all users), and the admin-only Data Management pages (Bowlers full CRUD, Sessions create/close, Game Details frame-by-frame entry) are implemented end-to-end. A standalone Leaderboard page is not yet built — `GET /api/scores/leaderboard` is available but nothing in the UI calls it yet.
