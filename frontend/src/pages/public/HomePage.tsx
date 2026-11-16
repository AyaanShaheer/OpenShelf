import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { Search, ArrowRight, Book, FileText, GraduationCap, Newspaper } from 'lucide-react'
import { api } from '@/api/client'
import type { DashboardStats, Page, Resource } from '@/types/api'
import { useState } from 'react'

export function HomePage() {
  const navigate = useNavigate()
  const [q, setQ] = useState('')
  const recent = useQuery({ queryKey: ['recent'], queryFn: () => api.get<Resource[]>('/catalogue/recent') })
  const popular = useQuery({ queryKey: ['popular'], queryFn: () => api.get<Resource[]>('/catalogue/popular') })
  const stats = useQuery({ queryKey: ['public-stats'], queryFn: async () => {
    // derive public-facing stats from real backend data
    const page = await api.get<Page<Resource>>('/catalogue?page=0&size=1')
    const recentList = await api.get<Resource[]>('/catalogue/recent')
    return { total: page.totalElements ?? 0, recent: recentList?.length ?? 0 }
  }})

  const onSearch = (e: React.FormEvent) => {
    e.preventDefault()
    navigate(`/search?q=${encodeURIComponent(q)}`)
  }

  const categories = [
    { label: 'Books', type: 'BOOK', icon: Book },
    { label: 'Research Papers', type: 'RESEARCH_PAPER', icon: FileText },
    { label: 'Journals', type: 'JOURNAL', icon: Newspaper },
    { label: 'Theses', type: 'THESIS', icon: GraduationCap },
  ]

  return (
    <div>
      <section className="bg-gradient-to-b from-blue-950 to-blue-900 text-white">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-20 lg:py-28">
          <p className="text-blue-200 font-medium">Academic & Public Collections</p>
          <h1 className="mt-3 text-4xl sm:text-5xl font-semibold tracking-tight max-w-3xl">Discover trusted digital resources in one library platform</h1>
          <p className="mt-5 text-lg text-blue-100 max-w-2xl">Search books, journals, research papers, and theses with relevance-ranked discovery, rich metadata, and direct access to available resources.</p>
          <form onSubmit={onSearch} className="mt-8 flex max-w-2xl rounded-xl bg-white p-2 shadow-lg">
            <div className="flex items-center gap-2 flex-1 px-3 text-slate-500"><Search size={18} /><input value={q} onChange={e=>setQ(e.target.value)} placeholder="Search by title, author, subject, publisher, or keyword" className="w-full py-2 text-slate-900 placeholder:text-slate-400 outline-none"/></div>
            <button className="rounded-lg bg-blue-950 px-5 py-2.5 font-medium text-white hover:bg-blue-800">Search</button>
          </form>
          <div className="mt-8 flex flex-wrap gap-6 text-sm text-blue-100">
            <div><span className="text-white font-semibold text-lg">{stats.data?.total ?? '—'}</span> catalogue resources</div>
            <div><span className="text-white font-semibold text-lg">{stats.data?.recent ?? '—'}</span> recently added highlights</div>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-14">
        <div className="flex items-end justify-between mb-6">
          <div><h2 className="text-2xl font-semibold text-slate-900">Browse by category</h2><p className="text-slate-600 mt-1">Explore collections by resource type.</p></div>
        </div>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {categories.map(c => (
            <Link key={c.type} to={`/search?types=${c.type}`} className="group rounded-xl border border-slate-200 bg-white p-5 shadow-sm hover:shadow-md hover:border-blue-200 transition-all">
              <div className="h-10 w-10 rounded-lg bg-blue-50 text-blue-950 flex items-center justify-center"><c.icon size={18}/></div>
              <div className="mt-4 font-medium text-slate-900">{c.label}</div>
              <div className="mt-1 text-sm text-slate-500 inline-flex items-center gap-1 group-hover:text-blue-900">View resources <ArrowRight size={14}/></div>
            </Link>
          ))}
        </div>
      </section>

      {popular.data && popular.data.length > 0 && (
        <section className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 pb-8">
          <div className="flex items-end justify-between mb-6">
            <div><h2 className="text-2xl font-semibold text-slate-900">Popular resources</h2><p className="text-slate-600 mt-1">Most-viewed resources across the catalogue.</p></div>
            <Link to="/search" className="text-sm font-medium text-blue-950 hover:underline">Browse all</Link>
          </div>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {popular.data.slice(0,3).map(r => (
              <Link key={r.id} to={`/resources/${r.id}`} className="rounded-xl border border-blue-100 bg-white p-5 shadow-sm hover:shadow-md transition-shadow">
                <div className="text-xs font-medium uppercase tracking-wide text-blue-900">{r.resourceType || 'Resource'}</div>
                <h3 className="mt-2 font-semibold text-slate-900 line-clamp-2">{r.title}</h3>
                <p className="mt-1 text-sm text-slate-600">{(r.authors||[]).join(', ') || 'Unknown author'}</p>
              </Link>
            ))}
          </div>
        </section>
      )}

      <section className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 pb-8">
        <div className="flex items-end justify-between mb-6">
          <div><h2 className="text-2xl font-semibold text-slate-900">Recently added</h2><p className="text-slate-600 mt-1">Latest resources from the catalogue.</p></div>
          <Link to="/search?sort=date_desc" className="text-sm font-medium text-blue-950 hover:underline">View all</Link>
        </div>
        {recent.isLoading && <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">{Array.from({length:6}).map((_,i)=><div key={i} className="h-40 rounded-xl bg-slate-200 animate-pulse"/>)}</div>}
        {recent.isError && <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-red-700">Unable to load recent resources.</div>}
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {recent.data?.slice(0,6).map(r => (
            <Link key={r.id} to={`/resources/${r.id}`} className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm hover:shadow-md transition-shadow">
              <div className="text-xs font-medium uppercase tracking-wide text-blue-900">{r.resourceType || 'Resource'}</div>
              <h3 className="mt-2 font-semibold text-slate-900 line-clamp-2">{r.title}</h3>
              <p className="mt-1 text-sm text-slate-600">{(r.authors||[]).join(', ') || 'Unknown author'}</p>
              <p className="mt-2 text-sm text-slate-500">{r.publicationDate ? r.publicationDate.slice(0,4) : ''} {r.language ? `· ${r.language}` : ''}</p>
            </Link>
          ))}
        </div>
      </section>
    </div>
  )
}
