import { NavLink as RouterNavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  ActionIcon,
  AppShell,
  Box,
  Group,
  Menu,
  Avatar,
  Text,
  Burger,
  NavLink,
  useComputedColorScheme,
  useMantineColorScheme,
} from '@mantine/core'
import { useDisclosure } from '@mantine/hooks'
import { IconMoon, IconSun } from '@tabler/icons-react'
import { useAuth } from '../auth/AuthContext'
import { BowlingPins } from './BowlingPins'

export function Layout() {
  const { username, isAdmin, logout } = useAuth()
  const navigate = useNavigate()
  const [navOpened, { toggle: toggleNav }] = useDisclosure()
  const { setColorScheme } = useMantineColorScheme()
  const colorScheme = useComputedColorScheme('light')

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <AppShell header={{ height: 60 }} navbar={{ width: 220, breakpoint: 'sm', collapsed: { mobile: !navOpened } }}>
      <AppShell.Header>
        <Group h="100%" px="md" justify="space-between">
          <Group gap="xs">
            <Burger opened={navOpened} onClick={toggleNav} hiddenFrom="sm" size="sm" />
            <BowlingPins width={22} />
            <Text size="lg" fw={700}>Bowling Score Calculator</Text>
          </Group>
          <Group gap="xs">
            <ActionIcon
              variant="default"
              size="lg"
              radius="xl"
              aria-label="Toggle color scheme"
              onClick={() => setColorScheme(colorScheme === 'light' ? 'dark' : 'light')}
            >
              {colorScheme === 'light' ? <IconMoon size={18} /> : <IconSun size={18} />}
            </ActionIcon>
            <Menu shadow="md" width={180} position="bottom-end">
              <Menu.Target>
                <Group gap="xs" style={{ cursor: 'pointer' }}>
                  <Avatar radius="xl" color="lane">{username?.slice(0, 1).toUpperCase()}</Avatar>
                  <Text fw={500}>{username}</Text>
                </Group>
              </Menu.Target>
              <Menu.Dropdown>
                <Menu.Item component={RouterNavLink} to="/change-password">
                  Change password
                </Menu.Item>
                <Menu.Divider />
                <Menu.Item color="red" onClick={handleLogout}>
                  Log out
                </Menu.Item>
              </Menu.Dropdown>
            </Menu>
          </Group>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar p="md">
        <Text size="xs" fw={700} c="dimmed" mb={4}>
          GENERAL
        </Text>
        <NavLink component={RouterNavLink} to="/" end label="Sessions" onClick={toggleNav} />

        {isAdmin && (
          <>
            <Text size="xs" fw={700} c="dimmed" mt="md" mb={4}>
              DATA MANAGEMENT
            </Text>
            <NavLink
              component={RouterNavLink}
              to="/manage/bowlers"
              label="Add / Modify Bowler Details"
              onClick={toggleNav}
            />
            <NavLink
              component={RouterNavLink}
              to="/manage/sessions"
              label="Add / Modify Session Details"
              onClick={toggleNav}
            />
            <NavLink
              component={RouterNavLink}
              to="/manage/games"
              label="Add / Modify Game Details"
              onClick={toggleNav}
            />
          </>
        )}
      </AppShell.Navbar>

      <AppShell.Main>
        <Box p="md">
          <Outlet />
        </Box>
      </AppShell.Main>
    </AppShell>
  )
}
