import { useEffect, useState, type FormEvent } from 'react'
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

  if (loading) return <p>Loading bowlers...</p>
  if (error) return <p className="form-error">{error}</p>

  return (
    <div className="bowlers-page">
      <h1>Bowlers</h1>
      <table>
        <thead>
          <tr>
            <th>Name</th>
            {isAdmin && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {bowlers.map((bowler) => (
            <tr key={bowler.id}>
              <td>
                {editingId === bowler.id ? (
                  <input value={editingName} onChange={(e) => setEditingName(e.target.value)} />
                ) : (
                  bowler.name
                )}
              </td>
              {isAdmin && (
                <td>
                  {editingId === bowler.id ? (
                    <>
                      <button type="button" onClick={() => handleSaveEdit(bowler.id)}>Save</button>
                      <button type="button" onClick={() => setEditingId(null)}>Cancel</button>
                    </>
                  ) : (
                    <>
                      <button
                        type="button"
                        onClick={() => {
                          setEditingId(bowler.id)
                          setEditingName(bowler.name)
                        }}
                      >
                        Edit
                      </button>
                      <button type="button" onClick={() => handleDelete(bowler.id)}>Delete</button>
                    </>
                  )}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>

      {isAdmin && (
        <form className="add-bowler-form" onSubmit={handleCreate}>
          <input
            placeholder="New bowler name"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
          />
          <button type="submit">Add bowler</button>
        </form>
      )}
    </div>
  )
}
