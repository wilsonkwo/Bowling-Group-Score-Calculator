import { createContext, useContext, useState, type ReactNode } from 'react'
import { login as loginRequest } from '../api/auth'

interface AuthState {
  username: string | null
  roles: string[]
  isAdmin: boolean
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthState | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [username, setUsername] = useState<string | null>(() => localStorage.getItem('username'))
  const [roles, setRoles] = useState<string[]>(() => {
    const stored = localStorage.getItem('roles')
    return stored ? JSON.parse(stored) : []
  })

  async function login(loginUsername: string, password: string) {
    const result = await loginRequest(loginUsername, password)
    localStorage.setItem('token', result.token)
    localStorage.setItem('username', result.username)
    localStorage.setItem('roles', JSON.stringify(result.roles))
    setUsername(result.username)
    setRoles(result.roles)
  }

  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('roles')
    setUsername(null)
    setRoles([])
  }

  const value: AuthState = {
    username,
    roles,
    isAdmin: roles.includes('ROLE_ADMIN'),
    isAuthenticated: username !== null,
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
