import { useState, type FormEvent } from 'react'
import { Paper, Title, PasswordInput, Button, Alert, Stack, Container } from '@mantine/core'
import { notifications } from '@mantine/notifications'
import { changePassword } from '../api/auth'

export function ChangePasswordPage() {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)

    if (newPassword !== confirmPassword) {
      setError('New password and confirmation do not match')
      return
    }

    setSubmitting(true)
    try {
      await changePassword(currentPassword, newPassword)
      notifications.show({ color: 'green', title: 'Success', message: 'Password changed successfully.' })
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
    <Container size={420} mt="xl">
      <Paper component="form" onSubmit={handleSubmit} withBorder shadow="md" p={30} radius="md">
        <Title order={2} mb="md">
          Change password
        </Title>
        <Stack>
          <PasswordInput
            label="Current password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.currentTarget.value)}
            required
            autoFocus
          />
          <PasswordInput
            label="New password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.currentTarget.value)}
            required
            minLength={8}
          />
          <PasswordInput
            label="Confirm new password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.currentTarget.value)}
            required
            minLength={8}
          />
          {error && <Alert color="red">{error}</Alert>}
          <Button type="submit" loading={submitting} mt="sm">
            Change password
          </Button>
        </Stack>
      </Paper>
    </Container>
  )
}
