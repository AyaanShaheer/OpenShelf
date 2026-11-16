import { useQuery } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router-dom'
import { Search } from 'lucide-react'
import { api } from '@/api/client'
import type { Resource, SearchResponse } from '@/types/api'
import { useEffect, useMemo, useState } from 'react'

function useListParam(sp: URLSearchParams, key: string) {
  return sp.getAll(key)
}

export function SearchPage() {
  const [sp, setSp] = useSearchParams()
  const [qInput, setQInput] = useState(sp.get('q') || '')
  useEffect(() => setQInput(sp.get('q') || ''), [sp])

  const params = useMemo(() => {
    const p = new URLSearchParams()
    const q = sp.get('q'); if (q) p.set('q', q)
    sp.getAll('types').forEach(v => p.append('types', v))
    sp.getAll('languages').forEach(v => p.append('languages', v))
    sp.getAll('subjects').forEach(v => p.append('subjects', v))
    if (sp.get('sort')) p.set('sort', sp.get('sort')!)
    p.set('page', sp.get('page') || '0')
    p.set('size', sp.get('size') || '10')
    return p.toString()
  }, [sp])

  const search = useQuery({
    queryKey: ['search', params],
    queryFn: () => api.get<SearchResponse>(`/catalogue/search?${params}`),
  })

  const filtersMeta = useQuery({ queryKey: ['filters'], queryFn: () => api.get<any>('/catalogue/filters') })

  const toggle = (key: string, value: string) => {
    const next = new URLSearchParams(sp)
    const vals = next.getAll(key)
    next.delete(key)
    if (vals.includes(value)) vals.filter(v => v !== value).forEach(v => next.append(key, v))
    else [...vals, value].forEach(v => next.append(key, v))
    next.set('page', '0')
    setSp(next)
  }

  const submit = (e: React.FormEvent) => {
    e.preventDefault()
    const next = new URLSearchParams(sp)
    if (qInput) next.set('q', qInput); else next.delete('q')
    next.set('page', '0')
    setSp(next)
  }

  const setPage = (p: number) => { const next = new URLSearchParams(sp); next.set('page', String(p)); setSp(next) }
  const setSort = (s: string) => { const next = new URLSearchParams(sp); if (s) next.set('sort', s); else next.delete('sort'); next.set('page','0'); setSp(next) }

  const facets = search.data?.facets || {}
  const selectedTypes = useListParam(sp, 'types')
  const selectedLangs = useListParam(sp, 'languages')
  const selectedSubjects = useListParam(sp, 'subjects')
  const page = Number(sp.get('page') || '0')
  const size = Number(sp.get('size') || '10')
  const total = search.data?.total || 0
  const totalPages = Math.max(1, Math.ceil(total / size))

  const FacetGroup = ({ title, keyName, values, selected }: any) => (
    <div>
      <h3 className="text-sm font-semibold text-slate-900 mb-2">{title}</h3>
      <div className="space-y-1.5 max-h-48 overflow-auto pr-1">
        {(values || []).map((v: any) => {
          const val = v.value ?? v
          const count = v.count
          return (
            <label key={val} className="flex items-center gap-2 text-sm text-slate-700">
              <input type="checkbox" checked={selected.includes(val)} onChange={() => toggle(keyName, val)} className="rounded border-slate-300 text-blue-950" />
              <span className="flex-1 truncate">{val}</span>
              {count != null && <span className="text-slate-400">{count}</span>}
            </label>
          )
        })}
        {(!values || values.length === 0) && <p className="text-sm text-slate-400">No options</p>}
      </div>
    </div>
  )

  return (
    <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-6">
        <h1 className="text-3xl font-semibold text-slate-900">Catalogue search</h1>
        <p className="text-slate-600 mt-1">Search and filter digital resources across the collection.</p>
      </div>
      <form onSubmit={submit} className="mb-6 flex gap-3">
        <div className="flex-1 flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-4 py-2.5 shadow-sm">
          <Search size={18} className="text-slate-400" />
          <input value={qInput} onChange={e=>setQInput(e.target.value)} placeholder="Title, author, subject, publisher, keyword…" className="w-full outline-none" />
        </div>
        <button className="rounded-xl bg-blue-950 px-5 py-2.5 text-white font-medium hover:bg-blue-900">Search</button>
      </form>

      <div className="grid gap-6 lg:grid-cols-[280px_1fr]">
        <aside className="rounded-xl border border-slate-200 bg-white p-4 h-fit space-y-5">
          <div className="flex items-center justify-between"><h2 className="font-semibold">Filters</h2><button onClick={()=>setSp(qInput?{q:qInput}:{})} className="text-sm text-blue-900 hover:underline">Clear</button></div>
          <FacetGroup title="Resource type" keyName="types" values={facets.resource_type?.length ? facets.resource_type : (filtersMeta.data?.resourceTypes||[])} selected={selectedTypes} />
          <FacetGroup title="Language" keyName="languages" values={facets.language?.length ? facets.language : (filtersMeta.data?.languages||[])} selected={selectedLangs} />
          <FacetGroup title="Subject" keyName="subjects" values={facets.subjects?.length ? facets.subjects : (filtersMeta.data?.subjects||[])} selected={selectedSubjects} />
        </aside>

        <section>
          <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
            <div className="text-sm text-slate-600">{search.isLoading ? 'Searching…' : `${total} result${total===1?'':'s'}`}</div>
            <select value={sp.get('sort')||''} onChange={e=>setSort(e.target.value)} className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm">
              <option value="">Sort: Relevance / Recent</option>
              <option value="title_asc">Title (A–Z)</option>
              <option value="title_desc">Title (Z–A)</option>
              <option value="date_desc">Publication date (newest)</option>
              <option value="date_asc">Publication date (oldest)</option>
            </select>
          </div>

          {search.isLoading && <div className="space-y-3">{Array.from({length:5}).map((_,i)=><div key={i} className="h-28 rounded-xl bg-slate-200 animate-pulse"/>)}</div>}
          {search.isError && <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-red-700">Search is unavailable. Ensure the search service is running and try again.</div>}
          {search.data && search.data.results.length === 0 && <div className="rounded-xl border border-slate-200 bg-white p-10 text-center"><p className="font-medium text-slate-900">No results found</p><p className="text-slate-600 mt-1">Try different keywords or clear filters.</p></div>}

          <div className="space-y-3">
            {search.data?.results.map((r: Resource) => (
              <Link key={r.id} to={`/resources/${r.id}`} className="block rounded-xl border border-slate-200 bg-white p-5 shadow-sm hover:shadow-md transition-shadow">
                <div className="flex flex-wrap items-center gap-2 text-xs font-medium uppercase tracking-wide text-blue-900"><span>{r.resourceType || 'Resource'}</span>{r.language && <span className="text-slate-400 normal-case font-normal">· {r.language}</span>}</div>
                <h3 className="mt-1 text-lg font-semibold text-slate-900">{r.title}</h3>
                <p className="mt-1 text-sm text-slate-700">{(r.authors||[]).join(', ') || 'Unknown author'}{r.publicationDate ? ` · ${r.publicationDate.slice(0,4)}` : ''}{r.publisher ? ` · ${r.publisher}` : ''}</p>
                {r.description && <p className="mt-2 text-sm text-slate-600 line-clamp-2">{r.description}</p>}
                {(r.subjects||[]).length > 0 && <div className="mt-3 flex flex-wrap gap-2">{r.subjects!.slice(0,4).map(s => <span key={s} className="rounded-full bg-slate-100 px-2.5 py-1 text-xs text-slate-700">{s}</span>)}</div>}
              </Link>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="mt-6 flex items-center justify-center gap-2">
              <button disabled={page<=0} onClick={()=>setPage(page-1)} className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-sm disabled:opacity-40">Previous</button>
              <span className="text-sm text-slate-600">Page {page+1} of {totalPages}</span>
              <button disabled={page+1>=totalPages} onClick={()=>setPage(page+1)} className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-sm disabled:opacity-40">Next</button>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
