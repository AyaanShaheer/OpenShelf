import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { SearchPage } from './SearchPage'
import type { SearchResponse } from '@/types/api'

vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn((path: string) => {
      if (path.includes('/catalogue/filters')) {
        return Promise.resolve({ resourceTypes: ['BOOK'], languages: ['English'], subjects: ['Literature'] })
      }
      const body: SearchResponse = { results: [], total: 0, page: 0, size: 10, facets: {} }
      return Promise.resolve(body)
    }),
  },
}))

function renderSearch() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={qc}>
      <MemoryRouter initialEntries={['/search']}>
        <SearchPage />
      </MemoryRouter>
    </QueryClientProvider>
  )
}

describe('SearchPage', () => {
  it('shows an empty state when no results are returned', async () => {
    renderSearch()
    await waitFor(() => expect(screen.getByText(/no results found/i)).toBeInTheDocument())
    expect(screen.getByText(/0 results/i)).toBeInTheDocument()
  })
})
