import { api } from './client'

export interface Bowler {
  id: number
  name: string
}

export function getBowlers() {
  return api.get<Bowler[]>('/api/bowlers').then((r) => r.data)
}

export function createBowler(name: string) {
  return api.post<Bowler>('/api/bowlers', { name }).then((r) => r.data)
}

export function updateBowler(id: number, name: string) {
  return api.put<Bowler>(`/api/bowlers/${id}`, { name }).then((r) => r.data)
}

export function deleteBowler(id: number) {
  return api.delete(`/api/bowlers/${id}`)
}
