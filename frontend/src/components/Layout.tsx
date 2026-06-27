import { NavLink as RouterNavLink, Outlet, useNavigate } from 'react-router-dom'
import { AppShell, Box, Group, Menu, Avatar, Text, Burger, NavLink } from '@mantine/core'
import { useDisclosure } from '@mantine/hooks'
import { useAuth } from '../auth/AuthContext'

export function Layout() {
  const { username, logout } = useAuth()
  const navigate = useNavigate()
  const [navOpened, { toggle: toggleNav }] = useDisclosure()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <AppShell header={{ height: 60 }} navbar={{ width: 220, breakpoint: 'sm', collapsed: { mobile: !navOpened } }}>
      <AppShell.Header>
        <Group h="100%" px="md" justify="space-between">
          <Group>
            <Burger opened={navOpened} onClick={toggleNav} hiddenFrom="sm" size="sm" />
            <Text size="lg" fw={700}>Bowling Score Calculator</Text>
          </Group>
          <Menu shadow="md" width={180} position="bottom-end">
            <Menu.Target>
              <Group gap="xs" style={{ cursor: 'pointer' }}>
                <Avatar radius="xl" color="blue">{username?.slice(0, 1).toUpperCase()}</Avatar>
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
      </AppShell.Header>

      <AppShell.Navbar p="md">
        <NavLink
          component={RouterNavLink}
          to="/bowlers"
          label="Bowlers List"
          onClick={toggleNav}
        />
        <NavLink
          component={RouterNavLink}
          to="/sessions"
          label="Session List"
          onClick={toggleNav}
        />
        <NavLink
          component={RouterNavLink}
          to="/game-score"
          label="Add Game Score"
          onClick={toggleNav}
        />
      </AppShell.Navbar>

      <AppShell.Main>
        <Box p="md">
          <Outlet />
        </Box>
      </AppShell.Main>
    </AppShell>
  )
}
