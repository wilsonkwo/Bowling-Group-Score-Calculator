import { useEffect, useState, type FormEvent } from 'react'
import {
  Title,
  Table,
  Paper,
  Stack,
  Group,
  TextInput,
  Select,
  Button,
  Badge,
  Loader,
  Alert,
  Chip,
  Modal,
  Text,
} from '@mantine/core'
import { notifications } from '@mantine/notifications'
import { useAuth } from '../auth/AuthContext'
import {
  closeSession,
  createSession,
  deleteSession,
  getSessions,
  TIME_SLOT_LABELS,
  type BowlingSession,
  type TimeSlot,
} from '../api/sessions'

const TIME_SLOT_OPTIONS = (Object.keys(TIME_SLOT_LABELS) as TimeSlot[]).map((value) => ({
  value,
  label: TIME_SLOT_LABELS[value],
}))

export function SessionsPage() {
  const { isAdmin } = useAuth()
  const [sessions, setSessions] = useState<BowlingSession[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [sessionDate, setSessionDate] = useState('')
  const [timeSlot, setTimeSlot] = useState<TimeSlot | null>(null)
  const [location, setLocation] = useState('')
  const [notes, setNotes] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const [deleteTarget, setDeleteTarget] = useState<BowlingSession | null>(null)
  const [deleting, setDeleting] = useState(false)

  const [statusFilter, setStatusFilter] = useState<'ALL' | 'OPEN' | 'CLOSED'>('ALL')
  const filteredSessions = sessions.filter((s) => statusFilter === 'ALL' || s.status === statusFilter)

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
    if (!sessionDate || !timeSlot) return
    setSubmitting(true)
    try {
      await createSession(sessionDate, timeSlot, location || undefined, notes || undefined)
      setSessionDate('')
      setTimeSlot(null)
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

  async function handleDeleteConfirm() {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await deleteSession(deleteTarget.id)
      setDeleteTarget(null)
      loadSessions()
    } catch {
      notifications.show({ color: 'red', title: 'Error', message: 'Failed to delete session' })
    } finally {
      setDeleting(false)
    }
  }

  if (loading) return <Loader mt="xl" />
  if (error) return <Alert color="red">{error}</Alert>

  return (
    <Stack maw={700}>
      <Modal
        opened={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        title="Delete session"
        centered
      >
        <Text mb="md">
          Delete session on <strong>{deleteTarget?.sessionDate}</strong>
          {deleteTarget?.timeSlot ? ` (${TIME_SLOT_LABELS[deleteTarget.timeSlot]})` : ''}? This will
          permanently remove all associated games and scores.
        </Text>
        <Group justify="flex-end">
          <Button variant="default" onClick={() => setDeleteTarget(null)}>
            Cancel
          </Button>
          <Button color="red" loading={deleting} onClick={handleDeleteConfirm}>
            Delete
          </Button>
        </Group>
      </Modal>

      <Title order={2}>Sessions</Title>

      <Chip.Group value={statusFilter} onChange={(v) => setStatusFilter(v as 'ALL' | 'OPEN' | 'CLOSED')}>
        <Group gap="xs">
          <Chip value="ALL">All</Chip>
          <Chip value="OPEN">Open</Chip>
          <Chip value="CLOSED">Closed</Chip>
        </Group>
      </Chip.Group>

      <Paper withBorder shadow="xs">
        <Table verticalSpacing="sm" highlightOnHover>
          <Table.Thead>
            <Table.Tr>
              <Table.Th>Date</Table.Th>
              <Table.Th>Time Slot</Table.Th>
              <Table.Th>Location</Table.Th>
              <Table.Th>Status</Table.Th>
              {isAdmin && <Table.Th w={140}>Actions</Table.Th>}
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {filteredSessions.map((s) => (
              <Table.Tr key={s.id}>
                <Table.Td>{s.sessionDate}</Table.Td>
                <Table.Td>{s.timeSlot ? TIME_SLOT_LABELS[s.timeSlot] : '-'}</Table.Td>
                <Table.Td>{s.location ?? '-'}</Table.Td>
                <Table.Td>
                  <Badge color={s.status === 'OPEN' ? 'green' : 'gray'}>{s.status}</Badge>
                </Table.Td>
                {isAdmin && (
                  <Table.Td>
                    <Group gap="xs">
                      {s.status === 'OPEN' && (
                        <Button size="xs" variant="light" onClick={() => handleClose(s.id)}>
                          Close
                        </Button>
                      )}
                      <Button size="xs" color="red" variant="light" onClick={() => setDeleteTarget(s)}>
                        Delete
                      </Button>
                    </Group>
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
            <Select
              label="Time slot"
              placeholder="Select a time slot"
              data={TIME_SLOT_OPTIONS}
              value={timeSlot}
              onChange={(v) => setTimeSlot(v as TimeSlot | null)}
              required
              w={220}
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
