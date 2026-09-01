---
name: scoring-rules
description: Use for any work touching ScoreService, the Frame/BowlerGame entities, frame score entry (including frontend PinRack/frame submission), win/loss results, or the session leaderboard. Contains the exact scoring business rules and known persistence gotchas — tests must pin down these numbers.
---

# Scoring rules

Canonical reference: `okf/metrics/scoring.md`. Implementation: `src/main/java/sg/sports/bowling/service/ScoreService.java`. Any change here needs tests that assert the exact numbers (see `tdd-feature` skill) and an update to `okf/metrics/scoring.md` (see `okf-docs` skill).

## Frame score (standard ten-pin lookahead)

Computed per frame in `calculateFrameScore`:

- **Strike** (`ball1 == 10`): `10 + next two balls thrown`
- **Spare** (`ball1 + ball2 == 10`): `10 + next ball thrown`
- **Open frame**: `ball1 + ball2`
- **Frame 10**: simply the sum of the balls thrown (2 or 3) — no lookahead beyond the frame

`Frame.cumulative_score` is the running sum of `frame_score` through that frame; `BowlerGame.total_score` is the sum across all 10 frames.

## saveFrames semantics (persistence gotcha)

- `saveFrames` always **replaces** a bowler's full frame list for a game — the frontend resubmits the growing list every time a frame completes.
- The delete-then-insert is followed by an explicit `frameRepository.flush()`. Without it, Hibernate can order the delete and the new insert for the same `frame_number` within one flush incorrectly, violating the `(bowler_game_id, frame_number)` unique constraint. Do not remove that flush.
- Submitting frames creates the `BowlerGame` implicitly on first submit.

## Frame points (spare/strike bonus, independent of pin score)

- Strike: **2** points
- Spare: **1** point
- Open frame: **0** points

## Game points (group win/loss)

`recalculateResults` reruns for every `BowlerGame` in the game after **every** frame save:

- **Mid-game** (not every participant finished): `game_points` = sum of frame points so far; `result` stays `null`. Win/loss is **gated** on `isGameComplete` for every participant (all 10 frames, with enough balls: open frame needs 2, strike/spare needs ball3 where applicable). Without this gate, two bowlers tied on a partial score would get a misleading early `DRAW` and split bonus — do not weaken this gate.
- **All participants finished**:
  1. `maxScore` = highest `total_score`.
  2. Bowlers at `maxScore`: `WIN` if alone, `DRAW` if tied; each gets an equal share of a fixed **3-point** win bonus (`3 / numberOfWinners`).
  3. `game_points` = frame points + win-bonus share (winners/tied winners only); everyone else gets just frame points.

## Leaderboard

`GET /api/scores/leaderboard?sessionId=` sums each bowler's `game_points` across all games in the session (`BowlerGameRepository.findSessionLeaderboard`). There is an endpoint but no standalone frontend leaderboard page yet.
