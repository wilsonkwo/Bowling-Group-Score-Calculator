import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Title, Paper, Stack, Group, Badge, Loader, Alert, Table, Text, Button } from '@mantine/core'
import { getSession, getGames, TIME_SLOT_LABELS, type BowlingSession, type Game } from '../api/sessions'
import { getParticipants, type ParticipantResponse } from '../api/scores'

export function SessionDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [session, setSession] = useState<BowlingSession | null>(null)
  const [games, setGames] = useState<Game[]>([])
  const [participantsByGame, setParticipantsByGame] = useState<Record<number, ParticipantResponse[]>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    const sessionId = Number(id)
    setLoading(true)
    Promise.all([getSession(sessionId), getGames(sessionId)])
      .then(async ([sessionData, gamesData]) => {
        setSession(sessionData)
        setGames(gamesData)
        const entries = await Promise.all(
          gamesData.map(async (g) => [g.id, await getParticipants(g.id)] as const),
        )
        setParticipantsByGame(Object.fromEntries(entries))
      })
      .catch(() => setError('Failed to load session details'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <Loader mt="xl" />
  if (error) return <Alert color="red">{error}</Alert>
  if (!session) return null

  return (
    <Stack maw={800}>
      <Group justify="space-between">
        <Title order={2}>{session.sessionDate}</Title>
        <Button variant="subtle" onClick={() => navigate('/')}>
          ← Back to sessions
        </Button>
      </Group>

      <Paper withBorder shadow="xs" p="md">
        <Group gap="xl">
          <Text>
            <strong>Time slot:</strong> {session.timeSlot ? TIME_SLOT_LABELS[session.timeSlot] : '-'}
          </Text>
          <Text>
            <strong>Location:</strong> {session.location ?? '-'}
          </Text>
          <Badge color={session.status === 'OPEN' ? 'green' : 'gray'}>{session.status}</Badge>
        </Group>
        {session.notes && (
          <Text mt="sm" c="dimmed">
            {session.notes}
          </Text>
        )}
      </Paper>

      {games.length === 0 ? (
        <Text c="dimmed">No games recorded for this session yet.</Text>
      ) : (
        games.map((game) => (
          <Paper key={game.id} withBorder shadow="xs" p="md">
            <Title order={4} mb="sm">
              Game {game.gameNumber}
            </Title>
            <Table verticalSpacing="xs">
              <Table.Thead>
                <Table.Tr>
                  <Table.Th>Bowler</Table.Th>
                  <Table.Th>Total</Table.Th>
                  <Table.Th>Points</Table.Th>
                  <Table.Th>Result</Table.Th>
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {(participantsByGame[game.id] ?? []).map((p) => (
                  <Table.Tr key={p.bowlerId}>
                    <Table.Td>{p.bowlerName}</Table.Td>
                    <Table.Td>{p.totalScore ?? '-'}</Table.Td>
                    <Table.Td>{p.gamePoints}</Table.Td>
                    <Table.Td>
                      {p.result && (
                        <Badge color={p.result === 'WIN' ? 'green' : p.result === 'DRAW' ? 'yellow' : 'red'}>
                          {p.result}
                        </Badge>
                      )}
                    </Table.Td>
                  </Table.Tr>
                ))}
              </Table.Tbody>
            </Table>
          </Paper>
        ))
      )}
    </Stack>
  )
}
