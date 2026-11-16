import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { ResourceFormPage } from './ResourceFormPage'

const post = vi.fn(() => Promise.resolve({}))
vi.mock('@/api/client', () => ({
  ApiError: class extends Error {},
  api: {
    get: vi.fn(() => Promise.resolve({})),
    post: (...args: any[]) => post(...args),
    put: vi.fn(() => Promise.resolve({})),
  },
}))

function renderForm() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter><ResourceFormPage /></MemoryRouter>
    </QueryClientProvider>
  )
}

describe('ResourceFormPage', () => {
  it('validates required title', async () => {
    renderForm()
    fireEvent.click(screen.getByRole('button', { name: /save resource/i }))
    await waitFor(() => expect(screen.getByText(/title is required/i)).toBeInTheDocument())
  })

  it('submits a new resource', async () => {
    renderForm()
    fireEvent.change(screen.getByLabelText(/title/i), { target: { value: 'New Catalogue Resource' } })
    fireEvent.click(screen.getByRole('button', { name: /save resource/i }))
    await waitFor(() => expect(post).toHaveBeenCalled())
    const [path, payload] = post.mock.calls[0] as any
    expect(path).toBe('/admin/resources')
    expect(payload.title).toBe('New Catalogue Resource')
  })
})
