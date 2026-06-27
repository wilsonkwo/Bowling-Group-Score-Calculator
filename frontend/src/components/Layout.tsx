import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function Layout() {
  const { username, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  function handleLogout() {
    setMenuOpen(false)
    logout()
    navigate('/login')
  }

  function handleChangePassword() {
    setMenuOpen(false)
    navigate('/change-password')
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <nav>
          <NavLink to="/bowlers">Bowlers</NavLink>
        </nav>
        <div className="app-header-user">
          <button type="button" className="user-menu-trigger" onClick={() => setMenuOpen((open) => !open)}>
            {username} ▾
          </button>
          {menuOpen && (
            <div className="user-menu">
              <button type="button" onClick={handleChangePassword}>Change password</button>
              <button type="button" onClick={handleLogout}>Log out</button>
            </div>
          )}
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
