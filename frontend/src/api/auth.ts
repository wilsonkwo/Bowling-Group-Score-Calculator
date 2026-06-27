import { api } from './client'

export interface LoginResponse {
  token: string
  type: string
  username: string
  roles: string[]
}

export function login(username: string, password: string) {
  return api.post<LoginResponse>('/api/auth/login', { username, password }).then((r) => r.data)
}

export function register(username: string, email: string, password: string) {
  return api.post<{ message: string }>('/api/auth/register', { username, email, password }).then((r) => r.data)
}

export function changePassword(currentPassword: string, newPassword: string) {
  return api
    .post<{ message: string }>('/api/auth/change-password', { currentPassword, newPassword })
    .then((r) => r.data)
}
