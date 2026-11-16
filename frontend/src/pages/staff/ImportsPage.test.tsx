import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { ImportsPage } from './ImportsPage'

const preview = {
  importJobId: 12,
  filename: 'sample.xml',
  totalRecords: 2,
  validRecords: 1,
  invalidRecords: 1,
  duplicateRecords: 0,
  records: [
    { identifier: 'DOI-1', title: 'Valid Record', errors: [], duplicate: false },
    { identifier: 'DOI-2', title: 'Invalid Record', errors: ['Missing mandatory field: dc:title'], duplicate: false },
  ],
}

vi.mock('@/api/client', () => ({
  ApiError: class extends Error {},
  api: {
    get: vi.fn(() => Promise.resolve([])),
    post: vi.fn((path: string) => {
      if (path.includes('/preview')) return Promise.resolve(preview)
      return Promise.resolve({})
    }),
  },
}))

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter><ImportsPage /></MemoryRouter>
    </QueryClientProvider>
  )
}

describe('ImportsPage', () => {
  it('shows a preview with valid and invalid records', async () => {
    renderPage()
    const file = new File(['<metadata/>'], 'sample.xml', { type: 'text/xml' })
    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    fireEvent.change(input, { target: { files: [file] } })
    fireEvent.click(screen.getByRole('button', { name: /preview import/i }))

    await waitFor(() => expect(screen.getByText(/Preview: sample.xml/i)).toBeInTheDocument())
    expect(screen.getByText('Valid Record')).toBeInTheDocument()
    expect(screen.getByText(/Missing mandatory field: dc:title/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /commit valid records/i })).toBeEnabled()
  })
})
