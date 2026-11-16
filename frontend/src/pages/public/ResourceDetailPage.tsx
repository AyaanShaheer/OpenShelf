import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { api } from '@/api/client'
import type { Resource } from '@/types/api'
import { ArrowLeft, ExternalLink, BookOpen } from 'lucide-react'

export function ResourceDetailPage() {
  const { id } = useParams()
  const q = useQuery({ queryKey: ['resource', id], queryFn: () => api.get<Resource>(`/catalogue/${id}`), enabled: !!id })

  if (q.isLoading) return <div className="mx-auto max-w-4xl px-4 py-10"><div className="h-64 rounded-xl bg-slate-200 animate-pulse"/></div>
  if (q.isError || !q.data) return <div className="mx-auto max-w-4xl px-4 py-10"><div className="rounded-xl border border-red-200 bg-red-50 p-6 text-red-700">Resource not found or unavailable.</div></div>
  const r = q.data
  return (
    <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8 py-8">
      <Link to="/search" className="inline-flex items-center gap-1 text-sm text-slate-600 hover:text-slate-900 mb-4"><ArrowLeft size={16}/> Back to search</Link>
      <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <div className="text-xs font-medium uppercase tracking-wide text-blue-900">{r.resourceType || 'Resource'}</div>
        <h1 className="mt-2 text-3xl font-semibold text-slate-900">{r.title}</h1>
        <p className="mt-2 text-slate-700">{(r.authors||[]).join(', ') || 'Unknown author'}</p>
        {r.description && <p className="mt-5 text-slate-700 leading-relaxed">{r.description}</p>}

        <dl className="mt-8 grid gap-4 sm:grid-cols-2 text-sm">
          <div><dt className="text-slate-500">Publisher</dt><dd className="text-slate-900 mt-0.5">{r.publisher || '—'}</dd></div>
          <div><dt className="text-slate-500">Publication date</dt><dd className="text-slate-900 mt-0.5">{r.publicationDate || '—'}</dd></div>
          <div><dt className="text-slate-500">Language</dt><dd className="text-slate-900 mt-0.5">{r.language || '—'}</dd></div>
          <div><dt className="text-slate-500">Format</dt><dd className="text-slate-900 mt-0.5">{r.format || '—'}</dd></div>
          <div><dt className="text-slate-500">Identifier</dt><dd className="text-slate-900 mt-0.5">{r.isbnOrIdentifier || '—'}</dd></div>
          <div><dt className="text-slate-500">Rights</dt><dd className="text-slate-900 mt-0.5">{r.rights || '—'}</dd></div>
        </dl>

        {(r.subjects||[]).length > 0 && <div className="mt-6"><div className="text-sm text-slate-500 mb-2">Subjects</div><div className="flex flex-wrap gap-2">{r.subjects!.map(s => <span key={s} className="rounded-full bg-slate-100 px-3 py-1 text-sm text-slate-700">{s}</span>)}</div></div>}

        <div className="mt-8 flex flex-wrap gap-3">
          {r.sourceUrl ? (
            <a href={r.sourceUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-2 rounded-lg bg-blue-950 px-4 py-2.5 text-white hover:bg-blue-900"><ExternalLink size={16}/> Access resource</a>
          ) : (
            <span className="inline-flex items-center gap-2 rounded-lg bg-slate-100 px-4 py-2.5 text-slate-600"><BookOpen size={16}/> No digital link available</span>
          )}
        </div>
      </div>
    </div>
  )
}
