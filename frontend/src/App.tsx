import { Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { AdminRoute } from './components/AdminRoute'
import { LoginPage } from './pages/LoginPage'
import { BowlersPage } from './pages/BowlersPage'
import { ChangePasswordPage } from './pages/ChangePasswordPage'
import { SessionsPage } from './pages/SessionsPage'
import { SessionsOverviewPage } from './pages/SessionsOverviewPage'
import { SessionDetailPage } from './pages/SessionDetailPage'
import { GameScorePage } from './pages/GameScorePage'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<SessionsOverviewPage />} />
          <Route path="/sessions/:id" element={<SessionDetailPage />} />
          <Route path="/change-password" element={<ChangePasswordPage />} />
          <Route element={<AdminRoute />}>
            <Route path="/manage/bowlers" element={<BowlersPage />} />
            <Route path="/manage/sessions" element={<SessionsPage />} />
            <Route path="/manage/games" element={<GameScorePage />} />
          </Route>
          {/* legacy bookmarks/links */}
          <Route path="/bowlers" element={<Navigate to="/manage/bowlers" replace />} />
          <Route path="/sessions" element={<Navigate to="/manage/sessions" replace />} />
          <Route path="/game-score" element={<Navigate to="/manage/games" replace />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App
