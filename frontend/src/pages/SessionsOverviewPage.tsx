import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Title, Table, Paper, Stack, Group, Chip, Badge, Loader, Alert } from '@mantine/core'
import { getSessions, TIME_SLOT_LABELS, type BowlingSession } from '../api/sessions'

/** Read-only, all-users session browser — admins manage sessions separately under Data Management. */
export function SessionsOverviewPage() {
  const navigate = useNavigate()
  const [sessions, setSessions] = useState<BowlingSession[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'OPEN' | 'CLOSED'>('ALL')

  useEffect(() => {
    getSessions()
      .then((data) => setSessions([...data].sort((a, b) => b.sessionDate.localeCompare(a.sessionDate))))
      .catch(() => setError('Failed to load sessions'))
      .finally(() => setLoading(false))
  }, [])

  const filteredSessions = sessions.filter((s) => statusFilter === 'ALL' || s.status === statusFilter)

  if (loading) return <Loader mt="xl" />
  if (error) return <Alert color="red">{error}</Alert>

  return (
    <Stack maw={700}>
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
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {filteredSessions.map((s) => (
              <Table.Tr key={s.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/sessions/${s.id}`)}>
                <Table.Td>{s.sessionDate}</Table.Td>
                <Table.Td>{s.timeSlot ? TIME_SLOT_LABELS[s.timeSlot] : '-'}</Table.Td>
                <Table.Td>{s.location ?? '-'}</Table.Td>
                <Table.Td>
                  <Badge color={s.status === 'OPEN' ? 'green' : 'gray'}>{s.status}</Badge>
                </Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Paper>
    </Stack>
  )
}
