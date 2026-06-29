import { useEffect, useState } from 'react'
import { Popover, UnstyledButton, Text, Stack, Button } from '@mantine/core'
import { PinRack } from './PinRack'

interface BallCellProps {
  value: number | undefined
  /** Display override for the raw value, e.g. "/" for a spare. */
  displayValue?: string
  /** Exactly which pins are still standing as this throw begins. */
  inPlayPins: Set<number>
  /** Exact pins left standing last time this ball was committed through the rack, if known. */
  priorStanding?: Set<number>
  disabled?: boolean
  opened: boolean
  onOpen: () => void
  onClose: () => void
  onCommit: (value: number, standingPins: Set<number>) => void
}

/**
 * A clickable ball-score cell: opens a pin rack to pick pins, defaulting to all-down.
 * Open/close is controlled by the parent so only one cell's rack is open at a time —
 * opening a different cell discards this one's unconfirmed pin picks instead of saving them.
 */
export function BallCell({
  value,
  displayValue,
  inPlayPins,
  priorStanding,
  disabled,
  opened,
  onOpen,
  onClose,
  onCommit,
}: BallCellProps) {
  const [standing, setStanding] = useState<Set<number>>(new Set())

  useEffect(() => {
    if (!opened) return
    if (priorStanding) {
      setStanding(new Set(priorStanding))
      return
    }
    if (value === undefined) {
      setStanding(new Set())
      return
    }
    // No tracked identity for an existing value (e.g. loaded from the backend) — best
    // guess is that the highest-numbered in-play pins are the ones left standing.
    const standingCount = inPlayPins.size - value
    const sorted = Array.from(inPlayPins).sort((a, b) => a - b)
    setStanding(new Set(sorted.slice(sorted.length - standingCount)))
  }, [opened])

  function togglePin(pinNumber: number) {
    setStanding((prev) => {
      const next = new Set(prev)
      if (next.has(pinNumber)) next.delete(pinNumber)
      else next.add(pinNumber)
      return next
    })
  }

  function handleUpdate() {
    onCommit(inPlayPins.size - standing.size, new Set(standing))
    onClose()
  }

  return (
    <Popover opened={opened} onClose={onClose} withArrow shadow="md" position="bottom">
      <Popover.Target>
        <UnstyledButton
          onClick={() => !disabled && onOpen()}
          disabled={disabled}
          w={32}
          h={32}
          style={{
            border: '1px solid var(--mantine-color-gray-4)',
            borderRadius: 4,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 600,
            cursor: disabled ? 'default' : 'pointer',
            background: disabled ? 'var(--mantine-color-gray-1)' : undefined,
            color: disabled ? 'var(--mantine-color-gray-5)' : undefined,
          }}
        >
          {displayValue ?? value ?? '–'}
        </UnstyledButton>
      </Popover.Target>
      <Popover.Dropdown>
        <Stack gap="xs" align="center">
          <PinRack inPlayPins={inPlayPins} standingPins={standing} onTogglePin={togglePin} />
          <Text size="xs" c="dimmed">
            {inPlayPins.size - standing.size} pins
          </Text>
          <Button size="xs" onClick={handleUpdate} fullWidth>
            Update
          </Button>
        </Stack>
      </Popover.Dropdown>
    </Popover>
  )
}
