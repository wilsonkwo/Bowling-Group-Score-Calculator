import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ActionIcon,
  Box,
  Center,
  Paper,
  Title,
  Text,
  TextInput,
  PasswordInput,
  Button,
  Alert,
  Stack,
  useComputedColorScheme,
  useMantineColorScheme,
} from '@mantine/core'
import { IconMoon, IconSun } from '@tabler/icons-react'
import { useAuth } from '../auth/AuthContext'
import { BowlingPins } from '../components/BowlingPins'

export function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const { setColorScheme } = useMantineColorScheme()
  const colorScheme = useComputedColorScheme('light')

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(username, password)
      navigate('/')
    } catch {
      setError('Invalid username or password')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box
      style={{
        minHeight: '100vh',
        background:
          colorScheme === 'light'
            ? 'linear-gradient(160deg, var(--mantine-color-lane-0) 0%, var(--mantine-color-gray-1) 60%, var(--mantine-color-lane-1) 100%)'
            : 'linear-gradient(160deg, var(--mantine-color-dark-8) 0%, var(--mantine-color-dark-9) 100%)',
      }}
    >
      <ActionIcon
        variant="default"
        size="lg"
        radius="xl"
        aria-label="Toggle color scheme"
        onClick={() => setColorScheme(colorScheme === 'light' ? 'dark' : 'light')}
        style={{ position: 'absolute', top: 16, right: 16 }}
      >
        {colorScheme === 'light' ? <IconMoon size={18} /> : <IconSun size={18} />}
      </ActionIcon>
      <Center mih="100vh">
        <Paper component="form" onSubmit={handleSubmit} withBorder shadow="md" p={30} radius="md" w={380}>
          <Center mb="xs">
            <BowlingPins width={110} />
          </Center>
          <Title order={2} ta="center">
            Bowling Score Calculator
          </Title>
          <Text ta="center" c="dimmed" size="sm" mb="md">
            Sign in to track your group sessions
          </Text>
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
    </Box>
  )
}
