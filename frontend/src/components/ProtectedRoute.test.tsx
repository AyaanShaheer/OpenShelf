import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { ProtectedRoute } from './ProtectedRoute'

const mockAuth = vi.fn()
vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => mockAuth() }))

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/login" element={<div>Login screen</div>} />
        <Route path="/staff" element={<ProtectedRoute><div>Staff content</div></ProtectedRoute>} />
        <Route path="/admin-only" element={<ProtectedRoute adminOnly><div>Admin content</div></ProtectedRoute>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('ProtectedRoute', () => {
  it('redirects unauthenticated users to login', () => {
    mockAuth.mockReturnValue({ user: null, loading: false, isAdmin: false })
    renderAt('/staff')
    expect(screen.getByText('Login screen')).toBeInTheDocument()
  })

  it('renders staff content for authenticated users', () => {
    mockAuth.mockReturnValue({ user: { id: 1, role: 'CATALOGUER' }, loading: false, isAdmin: false })
    renderAt('/staff')
    expect(screen.getByText('Staff content')).toBeInTheDocument()
  })

  it('blocks non-admin users from admin-only routes', () => {
    mockAuth.mockReturnValue({ user: { id: 1, role: 'CATALOGUER' }, loading: false, isAdmin: false })
    renderAt('/admin-only')
    expect(screen.getByText('Staff content')).toBeInTheDocument()
  })
})
