import { Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { BowlersPage } from './pages/BowlersPage'
import { ChangePasswordPage } from './pages/ChangePasswordPage'
import { SessionsPage } from './pages/SessionsPage'
import { GameScorePage } from './pages/GameScorePage'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/bowlers" element={<BowlersPage />} />
          <Route path="/sessions" element={<SessionsPage />} />
          <Route path="/game-score" element={<GameScorePage />} />
          <Route path="/change-password" element={<ChangePasswordPage />} />
          <Route path="/" element={<Navigate to="/bowlers" replace />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App
