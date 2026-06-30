import { api } from './client'

export type TimeSlot = 'MORNING' | 'AFTERNOON' | 'EVENING'

export const TIME_SLOT_LABELS: Record<TimeSlot, string> = {
  MORNING: 'Morning (8am-12pm)',
  AFTERNOON: 'Afternoon (1pm-6pm)',
  EVENING: 'Evening (8pm-12am)',
}

export interface BowlingSession {
  id: number
  sessionDate: string
  location?: string
  notes?: string
  status: 'OPEN' | 'CLOSED'
  timeSlot?: TimeSlot
}

export interface Game {
  id: number
  session: BowlingSession
  gameNumber: number
}

export function getSessions() {
  return api.get<BowlingSession[]>('/api/sessions').then((r) => r.data)
}

export function getOpenSessions() {
  return api.get<BowlingSession[]>('/api/sessions/open').then((r) => r.data)
}

export function getSession(id: number) {
  return api.get<BowlingSession>(`/api/sessions/${id}`).then((r) => r.data)
}

export function createSession(sessionDate: string, timeSlot: TimeSlot, location?: string, notes?: string) {
  return api.post<BowlingSession>('/api/sessions', { sessionDate, timeSlot, location, notes }).then((r) => r.data)
}

export function closeSession(id: number) {
  return api.post<BowlingSession>(`/api/sessions/${id}/close`).then((r) => r.data)
}

export function getGames(sessionId: number) {
  return api.get<Game[]>(`/api/sessions/${sessionId}/games`).then((r) => r.data)
}

export function addGame(sessionId: number) {
  return api.post<Game>(`/api/sessions/${sessionId}/games`).then((r) => r.data)
}

export function deleteSession(sessionId: number) {
  return api.delete(`/api/sessions/${sessionId}`)
}
