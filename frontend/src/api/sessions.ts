import { api } from './client'

export interface BowlingSession {
  id: number
  sessionDate: string
  location?: string
  notes?: string
  status: 'OPEN' | 'CLOSED'
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

export function createSession(sessionDate: string, location?: string, notes?: string) {
  return api.post<BowlingSession>('/api/sessions', { sessionDate, location, notes }).then((r) => r.data)
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
