import { useEffect, useState, type FormEvent } from 'react'
import {
  Title,
  Table,
  Paper,
  Stack,
  Group,
  TextInput,
  Button,
  Badge,
  Loader,
  Alert,
} from '@mantine/core'
import { notifications } from '@mantine/notifications'
import { useAuth } from '../auth/AuthContext'
import { closeSession, createSession, getSessions, type BowlingSession } from '../api/sessions'

export function SessionsPage() {
  const { isAdmin } = useAuth()
  const [sessions, setSessions] = useState<BowlingSession[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [sessionDate, setSessionDate] = useState('')
  const [location, setLocation] = useState('')
  const [notes, setNotes] = useState('')
  const [submitting, setSubmitting] = useState(false)

  function loadSessions() {
    setLoading(true)
    getSessions()
      .then((data) => setSessions([...data].sort((a, b) => b.sessionDate.localeCompare(a.sessionDate))))
      .catch(() => setError('Failed to load sessions'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadSessions()
  }, [])

  async function handleCreate(e: FormEvent) {
    e.preventDefault()
    if (!sessionDate) return
    setSubmitting(true)
    try {
      await createSession(sessionDate, location || undefined, notes || undefined)
      setSessionDate('')
      setLocation('')
      setNotes('')
      loadSessions()
    } catch {
      notifications.show({ color: 'red', title: 'Error', message: 'Failed to create session' })
    } finally {
      setSubmitting(false)
    }
  }

  async function handleClose(id: number) {
    try {
      await closeSession(id)
      loadSessions()
    } catch {
      notifications.show({ color: 'red', title: 'Error', message: 'Failed to close session' })
    }
  }

  if (loading) return <Loader mt="xl" />
  if (error) return <Alert color="red">{error}</Alert>

  return (
    <Stack maw={700}>
      <Title order={2}>Sessions</Title>

      <Paper withBorder shadow="xs">
        <Table verticalSpacing="sm" highlightOnHover>
          <Table.Thead>
            <Table.Tr>
              <Table.Th>Date</Table.Th>
              <Table.Th>Location</Table.Th>
              <Table.Th>Status</Table.Th>
              {isAdmin && <Table.Th w={100}>Actions</Table.Th>}
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {sessions.map((s) => (
              <Table.Tr key={s.id}>
                <Table.Td>{s.sessionDate}</Table.Td>
                <Table.Td>{s.location ?? '-'}</Table.Td>
                <Table.Td>
                  <Badge color={s.status === 'OPEN' ? 'green' : 'gray'}>{s.status}</Badge>
                </Table.Td>
                {isAdmin && (
                  <Table.Td>
                    {s.status === 'OPEN' && (
                      <Button size="xs" variant="light" onClick={() => handleClose(s.id)}>
                        Close
                      </Button>
                    )}
                  </Table.Td>
                )}
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Paper>

      {isAdmin && (
        <Paper withBorder shadow="xs" p="md">
          <Title order={4} mb="sm">
            New session
          </Title>
          <Group component="form" onSubmit={handleCreate} align="flex-end" wrap="wrap">
            <TextInput
              label="Date"
              type="date"
              value={sessionDate}
              onChange={(e) => setSessionDate(e.currentTarget.value)}
              required
            />
            <TextInput
              label="Location"
              placeholder="Optional"
              value={location}
              onChange={(e) => setLocation(e.currentTarget.value)}
            />
            <TextInput
              label="Notes"
              placeholder="Optional"
              value={notes}
              onChange={(e) => setNotes(e.currentTarget.value)}
            />
            <Button type="submit" loading={submitting}>
              Create session
            </Button>
          </Group>
        </Paper>
      )}
    </Stack>
  )
}
