import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, ApiError } from '@/api/client'
import { useState } from 'react'

export function IndexingPage() {
  const qc = useQueryClient()
  const [msg, setMsg] = useState<string|null>(null)
  const q = useQuery({ queryKey: ['indexing'], queryFn: () => api.get<any>('/admin/indexing/status') })
  const reindex = useMutation({
    mutationFn: () => api.post<any>('/admin/indexing/reindex'),
    onSuccess: (data) => { setMsg(`Reindex completed: ${data.indexed} indexed, ${data.failed} failed.`); qc.invalidateQueries({ queryKey: ['indexing'] }); qc.invalidateQueries({ queryKey: ['dashboard'] }) },
    onError: (e: any) => setMsg(e instanceof ApiError ? e.message : 'Reindex failed'),
  })
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div><h1 className="text-2xl font-semibold text-slate-900">Search indexing</h1><p className="text-slate-600 mt-1">Monitor Solr indexing status and trigger re-indexing from PostgreSQL.</p></div>
        <button onClick={()=>reindex.mutate()} disabled={reindex.isPending} className="rounded-lg bg-blue-950 px-4 py-2 text-white hover:bg-blue-900 disabled:opacity-60">{reindex.isPending ? 'Reindexing…' : 'Reindex all'}</button>
      </div>
      {msg && <div className="rounded-lg border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700">{msg}</div>}
      {q.isLoading && <div className="h-40 bg-slate-200 rounded-xl animate-pulse"/>}
      {q.data && (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            {[['Indexed', q.data.indexed],['Pending', q.data.pending],['Failed', q.data.failed]].map(([k,v])=> (
              <div key={String(k)} className="rounded-xl border border-slate-200 bg-white p-5"><div className="text-sm text-slate-500">{k}</div><div className="mt-1 text-2xl font-semibold text-slate-900">{v}</div></div>
            ))}
          </div>
          <div className="rounded-xl border border-slate-200 bg-white p-5">
            <h2 className="font-semibold text-slate-900 mb-3">Recent failures</h2>
            {(!q.data.failures || q.data.failures.length===0) ? <p className="text-sm text-slate-500">No indexing failures recorded.</p> : (
              <div className="overflow-auto"><table className="w-full text-sm"><thead className="bg-slate-50 text-slate-600"><tr><th className="px-3 py-2 text-left">Resource ID</th><th className="px-3 py-2 text-left">Last indexed</th><th className="px-3 py-2 text-left">Error</th></tr></thead>
              <tbody>{q.data.failures.map((f:any,i:number)=>(<tr key={i} className="border-t border-slate-100"><td className="px-3 py-2">{f.resourceId ?? '—'}</td><td className="px-3 py-2">{f.lastIndexedAt ? new Date(f.lastIndexedAt).toLocaleString() : '—'}</td><td className="px-3 py-2 text-slate-700">{f.lastError || '—'}</td></tr>))}</tbody></table></div>
            )}
          </div>
        </>
      )}
    </div>
  )
}
