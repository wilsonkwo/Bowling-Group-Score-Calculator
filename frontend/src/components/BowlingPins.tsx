const PIN_BODY = 'var(--mantine-color-white)'
const PIN_STROKE = 'var(--mantine-color-gray-4)'
const PIN_STRIPE = 'var(--mantine-color-pin-6)'

function Pin({ x, y }: { x: number; y: number }) {
  return (
    <g transform={`translate(${x} ${y})`}>
      <path
        d="M12 1 C7 1 4.5 5 4.5 9 C4.5 12 6 14 7 16 C5 19 3 23 3 29 C3 38 7 47 12 47 C17 47 21 38 21 29 C21 23 19 19 17 16 C18 14 19.5 12 19.5 9 C19.5 5 17 1 12 1 Z"
        fill={PIN_BODY}
        stroke={PIN_STROKE}
        strokeWidth="1"
      />
      <rect x="5.6" y="13.2" width="12.8" height="2" rx="1" fill={PIN_STRIPE} />
      <rect x="5.6" y="16.2" width="12.8" height="2" rx="1" fill={PIN_STRIPE} />
    </g>
  )
}

const RACK: Array<Array<[number, number]>> = [
  [[45, 0]],
  [
    [30, 34],
    [60, 34],
  ],
  [
    [15, 68],
    [45, 68],
    [75, 68],
  ],
  [
    [0, 102],
    [30, 102],
    [60, 102],
    [90, 102],
  ],
]

export function BowlingPins({ width = 160 }: { width?: number }) {
  return (
    <svg
      width={width}
      height={(width * 150) / 114}
      viewBox="0 0 114 150"
      role="img"
      aria-label="Bowling pin rack"
    >
      {RACK.flatMap((row, r) =>
        row.map(([x, y], i) => <Pin key={`${r}-${i}`} x={x} y={y} />),
      )}
    </svg>
  )
}
