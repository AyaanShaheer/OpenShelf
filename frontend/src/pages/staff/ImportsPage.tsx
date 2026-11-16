import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { api, ApiError } from '@/api/client'
import type { ImportJob, ImportPreviewResponse } from '@/types/api'
import { useState } from 'react'

export function ImportsPage() {
  const qc = useQueryClient()
  const [file, setFile] = useState<File | null>(null)
  const [preview, setPreview] = useState<ImportPreviewResponse | null>(null)
  const [msg, setMsg] = useState<string | null>(null)

  const history = useQuery({ queryKey: ['imports'], queryFn: () => api.get<ImportJob[]>('/admin/imports') })

  const previewMut = useMutation({
    mutationFn: async (f: File) => {
      const fd = new FormData(); fd.append('file', f)
      return api.post<ImportPreviewResponse>('/admin/imports/preview', fd)
    },
    onSuccess: (data) => { setPreview(data); setMsg('Preview generated. Review records before committing.'); qc.invalidateQueries({ queryKey: ['imports'] }) },
    onError: (e: any) => setMsg(e instanceof ApiError ? e.message : 'Preview failed'),
  })

  const commitMut = useMutation({
    mutationFn: (id: number) => api.post(`/admin/imports/${id}/commit`),
    onSuccess: () => { setMsg('Import commit processed.'); setPreview(null); setFile(null); qc.invalidateQueries({ queryKey: ['imports'] }); qc.invalidateQueries({ queryKey: ['dashboard'] }) },
    onError: (e: any) => setMsg(e instanceof ApiError ? e.message : 'Commit failed'),
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Dublin Core XML import</h1>
        <p className="text-slate-600 mt-1">Upload, validate, preview, and commit metadata records.</p>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <h2 className="font-semibold text-slate-900 mb-3">Upload XML</h2>
        <div className="flex flex-wrap items-center gap-3">
          <input type="file" accept=".xml,text/xml,application/xml" onChange={e=>{ setFile(e.target.files?.[0]||null); setPreview(null); setMsg(null) }} />
          <button disabled={!file || previewMut.isPending} onClick={()=>file && previewMut.mutate(file)} className="rounded-lg bg-blue-950 px-4 py-2 text-white disabled:opacity-50">{previewMut.isPending ? 'Validating…' : 'Preview import'}</button>
        </div>
        {msg && <div className="mt-3 text-sm text-slate-700">{msg}</div>}
      </div>

      {preview && (
        <div className="rounded-xl border border-slate-200 bg-white p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="font-semibold text-slate-900">Preview: {preview.filename}</h2>
              <p className="text-sm text-slate-600 mt-1">Total {preview.totalRecords} · Valid {preview.validRecords} · Invalid {preview.invalidRecords} · Duplicates {preview.duplicateRecords}</p>
            </div>
            <button disabled={commitMut.isPending || preview.validRecords===0} onClick={()=>commitMut.mutate(preview.importJobId)} className="rounded-lg bg-green-700 px-4 py-2 text-white hover:bg-green-800 disabled:opacity-50">{commitMut.isPending ? 'Committing…' : 'Commit valid records'}</button>
          </div>
          <div className="mt-4 overflow-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-slate-600"><tr><th className="px-3 py-2 text-left">Identifier</th><th className="px-3 py-2 text-left">Title</th><th className="px-3 py-2 text-left">Status</th><th className="px-3 py-2 text-left">Issues</th></tr></thead>
              <tbody>
                {preview.records.map((r, i) => (
                  <tr key={i} className="border-t border-slate-100">
                    <td className="px-3 py-2">{r.identifier || '—'}</td>
                    <td className="px-3 py-2">{r.title || '—'}</td>
                    <td className="px-3 py-2">{r.errors.length===0 ? <span className="text-green-700">Valid</span> : r.duplicate ? <span className="text-amber-700">Duplicate</span> : <span className="text-red-700">Invalid</span>}</td>
                    <td className="px-3 py-2 text-slate-600">{r.errors.join('; ') || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <h2 className="font-semibold text-slate-900 mb-3">Import history</h2>
        {history.isLoading && <div className="h-32 bg-slate-100 rounded animate-pulse"/>}
        {history.data && (
          <div className="overflow-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-slate-600"><tr><th className="px-3 py-2 text-left">ID</th><th className="px-3 py-2 text-left">File</th><th className="px-3 py-2 text-left">Status</th><th className="px-3 py-2 text-left">Counts</th><th className="px-3 py-2 text-left">Started</th><th></th></tr></thead>
              <tbody>
                {history.data.map(j => (
                  <tr key={j.id} className="border-t border-slate-100">
                    <td className="px-3 py-2">{j.id}</td>
                    <td className="px-3 py-2">{j.filename}</td>
                    <td className="px-3 py-2">{j.status}</td>
                    <td className="px-3 py-2 text-slate-600">ok {j.successfulRecords} / fail {j.failedRecords} / dup {j.duplicateRecords}</td>
                    <td className="px-3 py-2 text-slate-600">{j.startedAt ? new Date(j.startedAt).toLocaleString() : '—'}</td>
                    <td className="px-3 py-2 text-right"><Link to={`/staff/imports/${j.id}`} className="text-blue-900 hover:underline">Details</Link></td>
                  </tr>
                ))}
                {history.data.length===0 && <tr><td colSpan={6} className="px-3 py-8 text-center text-slate-500">No import jobs yet.</td></tr>}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
