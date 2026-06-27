import { useEffect, useState } from 'react'
import {
  Title,
  Select,
  Button,
  Group,
  Stack,
  Paper,
  Table,
  NumberInput,
  MultiSelect,
  Badge,
  Loader,
  Alert,
  Text,
  ScrollArea,
} from '@mantine/core'
import { notifications } from '@mantine/notifications'
import { useAuth } from '../auth/AuthContext'
import { getBowlers, type Bowler } from '../api/bowlers'
import { addGame, getGames, getOpenSessions, type BowlingSession, type Game } from '../api/sessions'
import { getParticipants, submitFrames, type ParticipantResponse } from '../api/scores'
import {
  framesToSubmit,
  isThirdBallEnabled,
  maxBall2,
  maxBall3,
  type FrameInput,
} from '../utils/frameRules'

const FRAME_NUMBERS = Array.from({ length: 10 }, (_, i) => i + 1)

export function GameScorePage() {
  const { isAdmin } = useAuth()

  const [sessions, setSessions] = useState<BowlingSession[]>([])
  const [sessionId, setSessionId] = useState<string | null>(null)

  const [games, setGames] = useState<Game[]>([])
  const [gameId, setGameId] = useState<string | null>(null)

  const [bowlers, setBowlers] = useState<Bowler[]>([])
  const [bowlerIds, setBowlerIds] = useState<string[]>([])
  const [scores, setScores] = useState<Record<number, Record<number, FrameInput>>>({})
  const [participants, setParticipants] = useState<ParticipantResponse[]>([])

  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getOpenSessions().then(setSessions).catch(() => setError('Failed to load sessions'))
    getBowlers().then(setBowlers).catch(() => setError('Failed to load bowlers'))
  }, [])

  useEffect(() => {
    if (!sessionId) {
      setGames([])
      setGameId(null)
      return
    }
    getGames(Number(sessionId)).then(setGames).catch(() => setError('Failed to load games'))
    setGameId(null)
  }, [sessionId])

  useEffect(() => {
    if (!gameId) {
      setBowlerIds([])
      setScores({})
      setParticipants([])
      return
    }
    loadParticipants(Number(gameId))
  }, [gameId])

  function loadParticipants(id: number) {
    setLoading(true)
    getParticipants(id)
      .then((data) => {
        setParticipants(data)
        setBowlerIds(data.map((p) => String(p.bowlerId)))
        const nextScores: Record<number, Record<number, FrameInput>> = {}
        for (const p of data) {
          const byFrame: Record<number, FrameInput> = {}
          for (const f of p.frames) {
            byFrame[f.frameNumber] = {
              ball1: f.ball1 ?? undefined,
              ball2: f.ball2 ?? undefined,
              ball3: f.ball3 ?? undefined,
            }
          }
          nextScores[p.bowlerId] = byFrame
        }
        setScores(nextScores)
      })
      .catch(() => setError('Failed to load scores'))
      .finally(() => setLoading(false))
  }

  async function handleAddGame() {
    if (!sessionId) return
    try {
      const game = await addGame(Number(sessionId))
      const updated = await getGames(Number(sessionId))
      setGames(updated)
      setGameId(String(game.id))
    } catch {
      notifications.show({ color: 'red', title: 'Error', message: 'Failed to add game' })
    }
  }

  function handleBowlerSelectionChange(ids: string[]) {
    setBowlerIds(ids)
    setScores((prev) => {
      const next = { ...prev }
      for (const id of ids) {
        const numId = Number(id)
        if (!next[numId]) next[numId] = {}
      }
      return next
    })
  }

  function updateBall(bowlerId: number, frameNumber: number, field: 'ball1' | 'ball2' | 'ball3', value: number | undefined) {
    setScores((prev) => {
      const bowlerFrames = { ...(prev[bowlerId] ?? {}) }
      const frame = { ...(bowlerFrames[frameNumber] ?? {}) }
      frame[field] = value
      if (field === 'ball1' && value === 10 && frameNumber !== 10) {
        frame.ball2 = undefined
      }
      bowlerFrames[frameNumber] = frame
      return { ...prev, [bowlerId]: bowlerFrames }
    })
  }

  async function handleSave() {
    if (!gameId) return
    setSaving(true)
    try {
      for (const idStr of bowlerIds) {
        const bowlerId = Number(idStr)
        const frames = framesToSubmit(scores[bowlerId] ?? {})
        if (frames.length === 0) continue
        await submitFrames(bowlerId, Number(gameId), frames)
      }
      await loadParticipants(Number(gameId))
      notifications.show({ color: 'green', title: 'Saved', message: 'Scores saved' })
    } catch {
      notifications.show({ color: 'red', title: 'Error', message: 'Failed to save scores' })
    } finally {
      setSaving(false)
    }
  }

  const participantByBowlerId = new Map(participants.map((p) => [p.bowlerId, p]))

  function bowlerName(id: number) {
    return bowlers.find((b) => b.id === id)?.name ?? `#${id}`
  }

  return (
    <Stack maw={1100}>
      <Title order={2}>Add game score</Title>

      {error && <Alert color="red">{error}</Alert>}

      <Group align="flex-end">
        <Select
          label="Session"
          placeholder="Select an open session"
          data={sessions.map((s) => ({
            value: String(s.id),
            label: `${s.sessionDate}${s.location ? ' — ' + s.location : ''}`,
          }))}
          value={sessionId}
          onChange={setSessionId}
          w={280}
        />
        <Select
          label="Game"
          placeholder="Select a game"
          data={games.map((g) => ({ value: String(g.id), label: `Game ${g.gameNumber}` }))}
          value={gameId}
          onChange={setGameId}
          disabled={!sessionId}
          w={160}
        />
        {isAdmin && (
          <Button onClick={handleAddGame} disabled={!sessionId} variant="light">
            Add new game
          </Button>
        )}
      </Group>

      {gameId && (
        <>
          {isAdmin && (
            <MultiSelect
              label="Bowlers in this game"
              placeholder="Pick bowlers from the bowlers list"
              data={bowlers.map((b) => ({ value: String(b.id), label: b.name }))}
              value={bowlerIds}
              onChange={handleBowlerSelectionChange}
              maw={500}
            />
          )}

          {loading ? (
            <Loader />
          ) : bowlerIds.length === 0 ? (
            <Text c="dimmed">No bowlers added to this game yet.</Text>
          ) : (
            <Paper withBorder shadow="xs">
              <ScrollArea>
                <Table withColumnBorders verticalSpacing="xs">
                  <Table.Thead>
                    <Table.Tr>
                      <Table.Th>Bowler</Table.Th>
                      {FRAME_NUMBERS.map((n) => (
                        <Table.Th key={n} ta="center">
                          {n}
                        </Table.Th>
                      ))}
                      <Table.Th>Total</Table.Th>
                      <Table.Th>Points</Table.Th>
                      <Table.Th>Result</Table.Th>
                    </Table.Tr>
                  </Table.Thead>
                  <Table.Tbody>
                    {bowlerIds.map((idStr) => {
                      const bowlerId = Number(idStr)
                      const bowlerFrames = scores[bowlerId] ?? {}
                      const participant = participantByBowlerId.get(bowlerId)
                      return (
                        <Table.Tr key={bowlerId}>
                          <Table.Td fw={500}>{bowlerName(bowlerId)}</Table.Td>
                          {FRAME_NUMBERS.map((frameNumber) => {
                            const frame = bowlerFrames[frameNumber] ?? {}
                            const isTenth = frameNumber === 10
                            const ball1IsStrike = frame.ball1 === 10
                            return (
                              <Table.Td key={frameNumber}>
                                <Group gap={2} wrap="nowrap" justify="center">
                                  <NumberInput
                                    value={frame.ball1 ?? ''}
                                    onChange={(v) =>
                                      updateBall(bowlerId, frameNumber, 'ball1', typeof v === 'number' ? v : undefined)
                                    }
                                    min={0}
                                    max={10}
                                    w={40}
                                    size="xs"
                                    hideControls
                                    disabled={!isAdmin}
                                  />
                                  {!(ball1IsStrike && !isTenth) && (
                                    <NumberInput
                                      value={frame.ball2 ?? ''}
                                      onChange={(v) =>
                                        updateBall(bowlerId, frameNumber, 'ball2', typeof v === 'number' ? v : undefined)
                                      }
                                      min={0}
                                      max={maxBall2(frame.ball1)}
                                      w={40}
                                      size="xs"
                                      hideControls
                                      disabled={!isAdmin || frame.ball1 === undefined}
                                    />
                                  )}
                                  {isTenth && isThirdBallEnabled(frameNumber, frame) && (
                                    <NumberInput
                                      value={frame.ball3 ?? ''}
                                      onChange={(v) =>
                                        updateBall(bowlerId, frameNumber, 'ball3', typeof v === 'number' ? v : undefined)
                                      }
                                      min={0}
                                      max={maxBall3(frame)}
                                      w={40}
                                      size="xs"
                                      hideControls
                                      disabled={!isAdmin}
                                    />
                                  )}
                                </Group>
                              </Table.Td>
                            )
                          })}
                          <Table.Td ta="center">{participant?.totalScore ?? '-'}</Table.Td>
                          <Table.Td ta="center">{participant?.gamePoints ?? '-'}</Table.Td>
                          <Table.Td ta="center">
                            {participant?.result && (
                              <Badge
                                color={
                                  participant.result === 'WIN'
                                    ? 'green'
                                    : participant.result === 'DRAW'
                                      ? 'yellow'
                                      : 'red'
                                }
                              >
                                {participant.result}
                              </Badge>
                            )}
                          </Table.Td>
                        </Table.Tr>
                      )
                    })}
                  </Table.Tbody>
                </Table>
              </ScrollArea>
            </Paper>
          )}

          {isAdmin && bowlerIds.length > 0 && (
            <Button onClick={handleSave} loading={saving} w={200}>
              Save scores
            </Button>
          )}
        </>
      )}
    </Stack>
  )
}
