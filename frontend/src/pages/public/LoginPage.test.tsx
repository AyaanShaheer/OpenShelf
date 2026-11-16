import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi } from 'vitest'
import { LoginPage } from './LoginPage'

vi.mock('@/contexts/AuthContext', () => ({
  useAuth: () => ({ login: vi.fn(), user: null, loading: false })
}))

describe('LoginPage', () => {
  it('renders sign in form', () => {
    render(<MemoryRouter><LoginPage /></MemoryRouter>)
    expect(screen.getByRole('heading', { name: /staff sign in/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
  })
})
