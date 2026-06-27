import { useState, type FormEvent } from 'react'
import { changePassword } from '../api/auth'

export function ChangePasswordPage() {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSuccess(false)

    if (newPassword !== confirmPassword) {
      setError('New password and confirmation do not match')
      return
    }

    setSubmitting(true)
    try {
      await changePassword(currentPassword, newPassword)
      setSuccess(true)
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Failed to change password'
      setError(message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="change-password-page">
      <h1>Change password</h1>
      <form className="change-password-form" onSubmit={handleSubmit}>
        <label>
          Current password
          <input
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            required
            autoFocus
          />
        </label>
        <label>
          New password
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
            minLength={8}
          />
        </label>
        <label>
          Confirm new password
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            minLength={8}
          />
        </label>
        {error && <p className="form-error">{error}</p>}
        {success && <p className="form-success">Password changed successfully.</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Changing...' : 'Change password'}
        </button>
      </form>
    </div>
  )
}
