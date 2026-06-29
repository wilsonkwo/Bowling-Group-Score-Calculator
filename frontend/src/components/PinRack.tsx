const PIN_ROWS: number[][] = [[1], [2, 3], [4, 5, 6], [7, 8, 9, 10]]
const PIN_ROWS_BACK_TO_FRONT = [...PIN_ROWS].reverse()

interface PinRackProps {
  /** Exactly which pins are still standing as this throw begins. */
  inPlayPins: Set<number>
  /** Pin numbers (1-10) the bowler left standing this throw. */
  standingPins: Set<number>
  onTogglePin: (pinNumber: number) => void
}

/**
 * Clickable 1-2-3-4 triangular pin deck, always showing the full 10-pin layout.
 * A pin starts "down" (knocked over) by default — clicking it marks it as still
 * standing. This matches how the data is entered fastest: most experienced bowlers
 * clear 6+ pins per throw, so starting from a strike and un-marking the few misses
 * beats building up from zero. Pins already knocked down by an earlier throw this
 * frame (not in inPlayPins) are greyed out and not clickable — they're shown for
 * context but aren't part of this throw.
 */
export function PinRack({ inPlayPins, standingPins, onTogglePin }: PinRackProps) {
  return (
    <div>
      {PIN_ROWS_BACK_TO_FRONT.map((row, i) => (
        <div key={i} style={{ display: 'flex', justifyContent: 'center', gap: 6, marginBottom: 6 }}>
          {row.map((pinNumber) => {
            const alreadyDown = !inPlayPins.has(pinNumber)
            const standing = !alreadyDown && standingPins.has(pinNumber)
            return (
              <button
                key={pinNumber}
                type="button"
                disabled={alreadyDown}
                onClick={() => onTogglePin(pinNumber)}
                title={
                  alreadyDown
                    ? `Pin ${pinNumber} (already down)`
                    : standing
                      ? `Pin ${pinNumber} (standing)`
                      : `Pin ${pinNumber} (down)`
                }
                style={{
                  width: 28,
                  height: 28,
                  borderRadius: '50%',
                  border: `2px solid ${alreadyDown ? 'var(--mantine-color-gray-4)' : 'var(--mantine-color-gray-6)'}`,
                  background: alreadyDown
                    ? 'var(--mantine-color-gray-2)'
                    : standing
                      ? 'var(--mantine-color-white)'
                      : 'var(--mantine-color-blue-6)',
                  color: alreadyDown
                    ? 'var(--mantine-color-gray-5)'
                    : standing
                      ? 'var(--mantine-color-gray-7)'
                      : 'white',
                  cursor: alreadyDown ? 'default' : 'pointer',
                  fontSize: 11,
                  fontWeight: 600,
                  padding: 0,
                  lineHeight: 1,
                }}
              >
                {pinNumber}
              </button>
            )
          })}
        </div>
      ))}
    </div>
  )
}
