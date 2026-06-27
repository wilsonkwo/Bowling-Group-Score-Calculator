import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { Center, Paper, Title, TextInput, PasswordInput, Button, Alert, Stack } from '@mantine/core'
import { useAuth } from '../auth/AuthContext'

export function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(username, password)
      navigate('/bowlers')
    } catch {
      setError('Invalid username or password')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Center mih="100vh">
      <Paper component="form" onSubmit={handleSubmit} withBorder shadow="md" p={30} radius="md" w={380}>
        <Title order={2} ta="center" mb="md">
          Bowling Score Calculator
        </Title>
        <Stack>
          <TextInput
            label="Username"
            value={username}
            onChange={(e) => setUsername(e.currentTarget.value)}
            required
            autoFocus
          />
          <PasswordInput
            label="Password"
            value={password}
            onChange={(e) => setPassword(e.currentTarget.value)}
            required
          />
          {error && <Alert color="red">{error}</Alert>}
          <Button type="submit" loading={submitting} fullWidth mt="sm">
            Log in
          </Button>
        </Stack>
      </Paper>
    </Center>
  )
}
