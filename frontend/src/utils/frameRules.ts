export interface FrameInput {
  ball1?: number
  ball2?: number
  ball3?: number
  /**
   * Pins left standing after this throw (frontend-only, not sent to the backend —
   * only used so the next throw's pin rack can highlight the exact pins still up,
   * instead of guessing from the raw count). Undefined when the value came from the
   * backend rather than being set through the rack in this session.
   */
  ball1Standing?: Set<number>
  ball2Standing?: Set<number>
  ball3Standing?: Set<number>
}

const ALL_PINS: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

export function fullPinSet(): Set<number> {
  return new Set(ALL_PINS)
}

/** Best-effort guess at which pins are still standing when we only know the count (no rack-tracked identity). */
function lowestPins(count: number): Set<number> {
  return new Set(ALL_PINS.slice(0, count))
}

/** Max pins allowed for ball2 of a frame, given ball1. */
export function maxBall2(ball1: number | undefined): number {
  if (ball1 === undefined) return 10
  return 10 - ball1
}

/** Whether the 10th frame's bonus 3rd ball is unlocked yet. */
export function isThirdBallEnabled(frameNumber: number, frame: FrameInput): boolean {
  if (frameNumber !== 10) return false
  if (frame.ball1 === undefined || frame.ball2 === undefined) return false
  return frame.ball1 === 10 || frame.ball1 + frame.ball2 === 10
}

/** Max pins allowed for the 10th frame's 3rd ball, given ball1/ball2 (pins re-rack on a strike). */
export function maxBall3(frame: FrameInput): number {
  if (frame.ball1 === 10 && frame.ball2 !== undefined && frame.ball2 < 10) {
    return 10 - frame.ball2
  }
  return 10
}

/** Exactly which pins are still standing as ball2 begins. */
export function inPlayPinsForBall2(frameNumber: number, frame: FrameInput): Set<number> {
  // 10th frame: a strike on ball1 re-racks a fresh 10 for ball2 (up to 3 throws total).
  if (frameNumber === 10 && frame.ball1 === 10) return fullPinSet()
  return frame.ball1Standing ?? lowestPins(maxBall2(frame.ball1))
}

/** Exactly which pins are still standing as the 10th frame's bonus 3rd ball begins. */
export function inPlayPinsForBall3(frame: FrameInput): Set<number> {
  if (frame.ball1 === 10 && frame.ball2 !== undefined && frame.ball2 < 10) {
    return frame.ball2Standing ?? lowestPins(maxBall3(frame))
  }
  return fullPinSet()
}

/** Whether ball2 completed a spare (two balls at a fresh rack summing to 10). */
export function isSpare(ball1: number | undefined, ball2: number | undefined): boolean {
  return ball1 !== undefined && ball1 !== 10 && ball2 !== undefined && ball1 + ball2 === 10
}

/** Whether a frame has enough balls entered to be considered "done" (no more input expected). */
export function isFrameComplete(frameNumber: number, frame: FrameInput): boolean {
  if (frame.ball1 === undefined) return false
  if (frameNumber !== 10) {
    return frame.ball1 === 10 || frame.ball2 !== undefined
  }
  // 10th frame
  if (frame.ball1 !== 10 && frame.ball1 + (frame.ball2 ?? -1) < 10 && frame.ball2 !== undefined) {
    return true // open frame, only 2 balls
  }
  return isThirdBallEnabled(10, frame) ? frame.ball3 !== undefined : false
}

/**
 * Build the ball arrays to submit to the backend for one bowler, stopping at the
 * first frame that isn't complete yet (frames must be contiguous from frame 1).
 */
export function framesToSubmit(frames: Record<number, FrameInput>): number[][] {
  const result: number[][] = []
  for (let frameNumber = 1; frameNumber <= 10; frameNumber++) {
    const frame = frames[frameNumber]
    if (!frame || !isFrameComplete(frameNumber, frame)) break

    if (frameNumber !== 10) {
      result.push(frame.ball1 === 10 ? [frame.ball1] : [frame.ball1 as number, frame.ball2 as number])
    } else {
      const balls = [frame.ball1 as number]
      if (frame.ball2 !== undefined) balls.push(frame.ball2)
      if (frame.ball3 !== undefined) balls.push(frame.ball3)
      result.push(balls)
    }
  }
  return result
}
