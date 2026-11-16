import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { api } from '@/api/client'
import type { Page, Resource } from '@/types/api'
import { Plus, Pencil, Archive } from 'lucide-react'
import { useState } from 'react'

export function ResourcesPage() {
  const [page, setPage] = useState(0)
  const qc = useQueryClient()
  const q = useQuery({ queryKey: ['admin-resources', page], queryFn: () => api.get<Page<Resource>>(`/admin/resources?page=${page}&size=20`) })
  const archive = useMutation({
    mutationFn: (id: number) => api.del(`/admin/resources/${id}`),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['admin-resources'] }); qc.invalidateQueries({ queryKey: ['dashboard'] }) },
  })

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <div><h1 className="text-2xl font-semibold text-slate-900">Catalogue management</h1><p className="text-slate-600 mt-1">Create, edit, and archive catalogue records.</p></div>
        <Link to="/staff/resources/new" className="inline-flex items-center gap-2 rounded-lg bg-blue-950 px-4 py-2 text-white hover:bg-blue-900"><Plus size={16}/> New resource</Link>
      </div>
      {q.isLoading && <div className="h-64 rounded-xl bg-slate-200 animate-pulse"/>}
      {q.isError && <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-red-700">Unable to load resources.</div>}
      {q.data && (
        <div className="rounded-xl border border-slate-200 bg-white overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600"><tr>
              <th className="text-left px-4 py-3 font-medium">Title</th>
              <th className="text-left px-4 py-3 font-medium">Type</th>
              <th className="text-left px-4 py-3 font-medium">Status</th>
              <th className="text-left px-4 py-3 font-medium">Year</th>
              <th className="px-4 py-3"></th>
            </tr></thead>
            <tbody>
              {q.data.content.map(r => (
                <tr key={r.id} className="border-t border-slate-100">
                  <td className="px-4 py-3"><div className="font-medium text-slate-900">{r.title}</div><div className="text-slate-500">{(r.authors||[]).join(', ')}</div></td>
                  <td className="px-4 py-3 text-slate-700">{r.resourceType}</td>
                  <td className="px-4 py-3"><span className={`inline-flex rounded-full px-2 py-0.5 text-xs font-medium ${r.status==='ACTIVE'?'bg-green-50 text-green-700':'bg-slate-100 text-slate-700'}`}>{r.status}</span></td>
                  <td className="px-4 py-3 text-slate-700">{r.publicationDate ? r.publicationDate.slice(0,4) : '—'}</td>
                  <td className="px-4 py-3 text-right whitespace-nowrap">
                    <Link to={`/staff/resources/${r.id}/edit`} className="inline-flex items-center gap-1 text-blue-900 hover:underline mr-3"><Pencil size={14}/> Edit</Link>
                    {r.status === 'ACTIVE' && <button onClick={()=>{ if(confirm('Archive this resource?')) archive.mutate(r.id) }} className="inline-flex items-center gap-1 text-slate-600 hover:text-red-700"><Archive size={14}/> Archive</button>}
                  </td>
                </tr>
              ))}
              {q.data.content.length === 0 && <tr><td colSpan={5} className="px-4 py-10 text-center text-slate-500">No resources yet.</td></tr>}
            </tbody>
          </table>
          <div className="flex items-center justify-between px-4 py-3 border-t border-slate-100 text-sm text-slate-600">
            <span>{q.data.totalElements} total</span>
            <div className="flex gap-2">
              <button disabled={page<=0} onClick={()=>setPage(p=>p-1)} className="rounded border border-slate-300 px-2 py-1 disabled:opacity-40">Prev</button>
              <span>Page {page+1} / {Math.max(1,q.data.totalPages)}</span>
              <button disabled={page+1>=q.data.totalPages} onClick={()=>setPage(p=>p+1)} className="rounded border border-slate-300 px-2 py-1 disabled:opacity-40">Next</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
