import { useEffect, useState, type FormEvent } from 'react'
import { Table, Title, TextInput, Button, Group, ActionIcon, Alert, Loader, Paper, Stack } from '@mantine/core'
import { IconPencil, IconTrash, IconCheck, IconX } from '@tabler/icons-react'
import { useAuth } from '../auth/AuthContext'
import { createBowler, deleteBowler, getBowlers, updateBowler, type Bowler } from '../api/bowlers'

export function BowlersPage() {
  const { isAdmin } = useAuth()
  const [bowlers, setBowlers] = useState<Bowler[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [newName, setNewName] = useState('')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editingName, setEditingName] = useState('')

  function loadBowlers() {
    setLoading(true)
    getBowlers()
      .then(setBowlers)
      .catch(() => setError('Failed to load bowlers'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadBowlers()
  }, [])

  async function handleCreate(e: FormEvent) {
    e.preventDefault()
    if (!newName.trim()) return
    await createBowler(newName.trim())
    setNewName('')
    loadBowlers()
  }

  async function handleSaveEdit(id: number) {
    if (!editingName.trim()) return
    await updateBowler(id, editingName.trim())
    setEditingId(null)
    loadBowlers()
  }

  async function handleDelete(id: number) {
    await deleteBowler(id)
    loadBowlers()
  }

  if (loading) return <Loader mt="xl" />
  if (error) return <Alert color="red">{error}</Alert>

  return (
    <Stack maw={600}>
      <Title order={2}>Bowlers</Title>
      <Paper withBorder shadow="xs">
        <Table verticalSpacing="sm" highlightOnHover>
          <Table.Thead>
            <Table.Tr>
              <Table.Th>Name</Table.Th>
              {isAdmin && <Table.Th w={120}>Actions</Table.Th>}
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {bowlers.map((bowler) => (
              <Table.Tr key={bowler.id}>
                <Table.Td>
                  {editingId === bowler.id ? (
                    <TextInput
                      value={editingName}
                      onChange={(e) => setEditingName(e.currentTarget.value)}
                      size="xs"
                      autoFocus
                    />
                  ) : (
                    bowler.name
                  )}
                </Table.Td>
                {isAdmin && (
                  <Table.Td>
                    {editingId === bowler.id ? (
                      <Group gap="xs">
                        <ActionIcon color="green" variant="light" onClick={() => handleSaveEdit(bowler.id)}>
                          <IconCheck size={16} />
                        </ActionIcon>
                        <ActionIcon color="gray" variant="light" onClick={() => setEditingId(null)}>
                          <IconX size={16} />
                        </ActionIcon>
                      </Group>
                    ) : (
                      <Group gap="xs">
                        <ActionIcon
                          variant="light"
                          onClick={() => {
                            setEditingId(bowler.id)
                            setEditingName(bowler.name)
                          }}
                        >
                          <IconPencil size={16} />
                        </ActionIcon>
                        <ActionIcon color="red" variant="light" onClick={() => handleDelete(bowler.id)}>
                          <IconTrash size={16} />
                        </ActionIcon>
                      </Group>
                    )}
                  </Table.Td>
                )}
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Paper>

      {isAdmin && (
        <Group component="form" onSubmit={handleCreate} align="flex-end">
          <TextInput
            placeholder="New bowler name"
            value={newName}
            onChange={(e) => setNewName(e.currentTarget.value)}
          />
          <Button type="submit">Add bowler</Button>
        </Group>
      )}
    </Stack>
  )
}
