import { createTheme, type MantineColorsTuple } from '@mantine/core'

// Lane: warm maple/amber inspired by polished bowling-lane wood.
const lane: MantineColorsTuple = [
  '#fff5e6',
  '#ffe8cc',
  '#ffd099',
  '#ffb664',
  '#ff9d38',
  '#f9840e',
  '#e67300',
  '#cc6200',
  '#b35500',
  '#8f4500',
]

// Pin: deep bowling-pin red used as the accent color.
const pin: MantineColorsTuple = [
  '#ffeaea',
  '#ffd2d2',
  '#ffa3a3',
  '#ff7070',
  '#f94545',
  '#e52e2e',
  '#d42121',
  '#bc1a1a',
  '#a11515',
  '#7d1010',
]

export const theme = createTheme({
  primaryColor: 'lane',
  colors: { lane, pin },
  defaultRadius: 'md',
  fontFamily:
    "'Segoe UI', system-ui, -apple-system, Roboto, 'Helvetica Neue', Arial, sans-serif",
  headings: {
    fontFamily:
      "'Segoe UI', system-ui, -apple-system, Roboto, 'Helvetica Neue', Arial, sans-serif",
    fontWeight: '700',
  },
})
