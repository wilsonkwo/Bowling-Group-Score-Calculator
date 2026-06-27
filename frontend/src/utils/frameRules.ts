export interface FrameInput {
  ball1?: number
  ball2?: number
  ball3?: number
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
