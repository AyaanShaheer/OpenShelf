import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { api } from '@/api/client'
import type { ImportJob } from '@/types/api'

export function ImportDetailPage() {
  const { id } = useParams()
  const q = useQuery({ queryKey: ['import', id], queryFn: () => api.get<ImportJob>(`/admin/imports/${id}`), enabled: !!id })
  if (q.isLoading) return <div className="h-64 rounded-xl bg-slate-200 animate-pulse"/>
  if (q.isError || !q.data) return <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-red-700">Import job not found.</div>
  const j = q.data
  return (
    <div>
      <Link to="/staff/imports" className="text-sm text-blue-900 hover:underline">← Back to imports</Link>
      <h1 className="mt-2 text-2xl font-semibold text-slate-900">Import job #{j.id}</h1>
      <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[['File', j.filename],['Status', j.status],['Imported by', j.importedBy || '—'],['Started', j.startedAt ? new Date(j.startedAt).toLocaleString() : '—'],['Completed', j.completedAt ? new Date(j.completedAt).toLocaleString() : '—'],['Total', j.totalRecords],['Successful', j.successfulRecords],['Failed/Dup', `${j.failedRecords} / ${j.duplicateRecords}`]].map(([k,v])=> (
          <div key={String(k)} className="rounded-xl border border-slate-200 bg-white p-4"><div className="text-sm text-slate-500">{k}</div><div className="mt-1 font-medium text-slate-900 break-all">{String(v)}</div></div>
        ))}
      </div>
      <div className="mt-6 rounded-xl border border-slate-200 bg-white p-5">
        <h2 className="font-semibold text-slate-900 mb-3">Errors & record issues</h2>
        {(!j.errors || j.errors.length===0) ? <p className="text-slate-500 text-sm">No errors recorded.</p> : (
          <ul className="space-y-2 text-sm">{j.errors.map((e,i)=><li key={i} className="rounded-lg bg-slate-50 px-3 py-2"><span className="font-medium text-slate-800">{e.recordIdentifier || 'Record'}:</span> <span className="text-slate-700">{e.errorMessage}</span></li>)}</ul>
        )}
      </div>
    </div>
  )
}
