import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { ResourceDetailPage } from './ResourceDetailPage'

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(() => Promise.resolve({
      id: 1, title: 'Open Access Futures', description: 'A study of open access.',
      resourceType: 'RESEARCH_PAPER', authors: ['Elena Morales'], subjects: ['Open Access'],
      publicationDate: '2023-05-10', sourceUrl: 'https://example.org/x', language: 'English', format: 'PDF',
    })),
  },
}))

function renderPage() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={['/resources/1']}>
        <Routes><Route path="/resources/:id" element={<ResourceDetailPage />} /></Routes>
      </MemoryRouter>
    </QueryClientProvider>
  )
}

describe('ResourceDetailPage', () => {
  it('renders resource metadata and access link', async () => {
    renderPage()
    await waitFor(() => expect(screen.getByText('Open Access Futures')).toBeInTheDocument())
    expect(screen.getByText('Elena Morales')).toBeInTheDocument()
    expect(screen.getByText('Open Access')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /access resource/i })).toHaveAttribute('href', 'https://example.org/x')
  })
})
