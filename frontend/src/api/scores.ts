import { api } from './client'

export interface FrameResponse {
  frameNumber: number
  ball1: number | null
  ball2: number | null
  ball3: number | null
  frameScore: number | null
  cumulativeScore: number | null
  framePoints: number
  strike: boolean
  spare: boolean
}

export interface ParticipantResponse {
  bowlerId: number
  bowlerName: string
  totalScore: number | null
  gamePoints: number
  result: 'WIN' | 'LOSS' | 'DRAW' | null
  frames: FrameResponse[]
}

export interface LeaderboardEntry {
  bowlerId: number
  bowlerName: string
  totalPoints: number
}

export function submitFrames(bowlerId: number, gameId: number, frames: number[][]) {
  return api
    .post<FrameResponse[]>('/api/scores/frames', { bowlerId, gameId, frames })
    .then((r) => r.data)
}

export function getParticipants(gameId: number) {
  return api.get<ParticipantResponse[]>(`/api/scores/games/${gameId}/participants`).then((r) => r.data)
}

export function getLeaderboard(sessionId: number) {
  return api.get<LeaderboardEntry[]>('/api/scores/leaderboard', { params: { sessionId } }).then((r) => r.data)
}
