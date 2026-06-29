---
type: Algorithm
title: Scoring
description: How frame scores, total scores, and win/loss group points are calculated.
resource: src/main/java/sg/sports/bowling/service/ScoreService.java
tags: [metrics, scoring]
timestamp: 2026-06-27T00:00:00Z
---
# Frame score
Standard ten-pin rules with lookahead, computed per frame in `calculateFrameScore`:
- **Strike** (`ball1 == 10`): `10 + next two balls thrown`.
- **Spare** (`ball1 + ball2 == 10`): `10 + next ball thrown`.
- **Open frame**: `ball1 + ball2`.
- **Frame 10**: simply the sum of whatever balls (2 or 3) were thrown.

`[Frame](../entities/frame.md).cumulative_score` is the running sum of `frame_score` through that frame; `[BowlerGame](../entities/bowler_game.md).total_score` is the sum across all 10 frames.

`saveFrames` always replaces a bowler's full frame list for a game — the frontend's frame-by-frame entry resubmits the growing list every time a frame completes (see [frontend.md](../frontend.md)). The delete-then-insert is followed by an explicit `frameRepository.flush()`: without it, Hibernate can queue the delete and the new insert for the same frame_number in the same flush in the wrong order, violating the `(bowler_game_id, frame_number)` unique constraint.

# Frame points (spare / strike bonus)
Each [Frame](../entities/frame.md) earns bonus points independent of its pin score:
- **Strike**: `2` points.
- **Spare**: `1` point.
- Open frame: `0` points.

# Game points (group win/loss)
After any bowler's frames are saved, `recalculateResults` reruns for every [BowlerGame](../entities/bowler_game.md) in that [Game](../entities/game.md). This runs after *every* frame save (the frontend resubmits the growing frame list each time a frame completes), so win/loss/draw is gated on every participant actually finishing all 10 frames (`isGameComplete` — last frame has enough balls for an open frame, or ball3 if it was a strike/spare) before it's decided. Without this gate, two bowlers merely tied on a partial score (e.g. both with just a frame-1 strike) would get a misleading early `DRAW` and a split win bonus.

- **Mid-game** (not every bowler done yet): every bowler's `game_points` = sum of their frame points so far (from spares/strikes); `result` stays `null`.
- **Once every bowler has finished all 10 frames**:
  1. Find `maxScore` among bowlers with a recorded `total_score`.
  2. Bowlers at `maxScore`: result is `WIN` if alone, `DRAW` if tied with others; each gets a share of a fixed **3-point** win bonus (`3 / numberOfWinners`).
  3. Every bowler's `game_points` = sum of their frame points + their win-bonus share if they won/tied for the win, else just their frame points.

# Leaderboard
`/api/scores/leaderboard?sessionId=` sums each bowler's `game_points` across all games in the session (`BowlerGameRepository.findSessionLeaderboard`), ranking the group standings for that outing.
