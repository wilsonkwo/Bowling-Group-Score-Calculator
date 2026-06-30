import { useEffect, useState } from 'react'
import {
  Title,
  Select,
  Button,
  Group,
  Stack,
  Paper,
  Table,
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
import { addGame, getGames, getOpenSessions, TIME_SLOT_LABELS, type BowlingSession, type Game } from '../api/sessions'
import { getParticipants, submitFrames, type ParticipantResponse } from '../api/scores'
import { BallCell } from '../components/BallCell'
import {
  framesToSubmit,
  fullPinSet,
  inPlayPinsForBall2,
  inPlayPinsForBall3,
  isFrameComplete,
  isSpare,
  isThirdBallEnabled,
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
  const [openCellKey, setOpenCellKey] = useState<string | null>(null)

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

  function updateBall(
    bowlerId: number,
    frameNumber: number,
    field: 'ball1' | 'ball2' | 'ball3',
    value: number | undefined,
    standingPins: Set<number>,
  ) {
    setScores((prev) => {
      const bowlerFrames = { ...(prev[bowlerId] ?? {}) }
      const frame = { ...(bowlerFrames[frameNumber] ?? {}) }
      frame[field] = value
      frame[`${field}Standing`] = standingPins
      // Changing an earlier ball invalidates whatever was already entered for later
      // balls in the frame (their max pins / in-play pins depend on this one).
      if (field === 'ball1') {
        frame.ball2 = undefined
        frame.ball2Standing = undefined
        frame.ball3 = undefined
        frame.ball3Standing = undefined
      } else if (field === 'ball2') {
        frame.ball3 = undefined
        frame.ball3Standing = undefined
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
            label: `${s.sessionDate} ${s.timeSlot ? TIME_SLOT_LABELS[s.timeSlot].split(' ')[0] : ''}`.trim(),
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
                            const previousFrameComplete =
                              frameNumber === 1 || isFrameComplete(frameNumber - 1, bowlerFrames[frameNumber - 1] ?? {})
                            return (
                              <Table.Td key={frameNumber}>
                                <Group gap={2} wrap="nowrap" justify="center">
                                  <BallCell
                                    value={frame.ball1}
                                    displayValue={frame.ball1 === 10 ? 'X' : undefined}
                                    inPlayPins={fullPinSet()}
                                    priorStanding={frame.ball1Standing}
                                    disabled={!isAdmin || !previousFrameComplete}
                                    opened={openCellKey === `${bowlerId}-${frameNumber}-ball1`}
                                    onOpen={() => setOpenCellKey(`${bowlerId}-${frameNumber}-ball1`)}
                                    onClose={() => setOpenCellKey(null)}
                                    onCommit={(v, pins) => updateBall(bowlerId, frameNumber, 'ball1', v, pins)}
                                  />
                                  {!(ball1IsStrike && !isTenth) && (
                                    <BallCell
                                      value={frame.ball2}
                                      displayValue={
                                        isSpare(frame.ball1, frame.ball2)
                                          ? '/'
                                          : frame.ball2 === 10
                                            ? 'X'
                                            : undefined
                                      }
                                      inPlayPins={inPlayPinsForBall2(frameNumber, frame)}
                                      priorStanding={frame.ball2Standing}
                                      disabled={!isAdmin || !previousFrameComplete || frame.ball1 === undefined}
                                      opened={openCellKey === `${bowlerId}-${frameNumber}-ball2`}
                                      onOpen={() => setOpenCellKey(`${bowlerId}-${frameNumber}-ball2`)}
                                      onClose={() => setOpenCellKey(null)}
                                      onCommit={(v, pins) => updateBall(bowlerId, frameNumber, 'ball2', v, pins)}
                                    />
                                  )}
                                  {isTenth && isThirdBallEnabled(frameNumber, frame) && (
                                    <BallCell
                                      value={frame.ball3}
                                      displayValue={
                                        frame.ball1 === 10 && isSpare(frame.ball2, frame.ball3)
                                          ? '/'
                                          : frame.ball3 === 10
                                            ? 'X'
                                            : undefined
                                      }
                                      inPlayPins={inPlayPinsForBall3(frame)}
                                      priorStanding={frame.ball3Standing}
                                      disabled={!isAdmin}
                                      opened={openCellKey === `${bowlerId}-${frameNumber}-ball3`}
                                      onOpen={() => setOpenCellKey(`${bowlerId}-${frameNumber}-ball3`)}
                                      onClose={() => setOpenCellKey(null)}
                                      onCommit={(v, pins) => updateBall(bowlerId, frameNumber, 'ball3', v, pins)}
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
