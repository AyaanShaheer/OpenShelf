import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, ApiError } from '@/api/client'
import type { UserSummary } from '@/types/api'
import { useState } from 'react'

const empty = { fullName: '', email: '', password: '', role: 'CATALOGUER' as const, active: true }

export function UsersPage() {
  const qc = useQueryClient()
  const q = useQuery({ queryKey: ['users'], queryFn: () => api.get<UserSummary[]>('/admin/users') })
  const [form, setForm] = useState<any>(empty)
  const [editing, setEditing] = useState<number | null>(null)
  const [msg, setMsg] = useState<string|null>(null)

  const save = useMutation({
    mutationFn: (payload: any) => editing ? api.put(`/admin/users/${editing}`, payload) : api.post('/admin/users', payload),
    onSuccess: () => { setMsg('Saved.'); setForm(empty); setEditing(null); qc.invalidateQueries({ queryKey: ['users'] }) },
    onError: (e: any) => setMsg(e instanceof ApiError ? e.message : 'Save failed'),
  })
  const del = useMutation({
    mutationFn: (id: number) => api.del(`/admin/users/${id}`),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }) },
    onError: (e: any) => setMsg(e instanceof ApiError ? e.message : 'Delete failed'),
  })

  const submit = (e: React.FormEvent) => {
    e.preventDefault()
    const payload = { ...form }
    if (editing && !payload.password) delete payload.password
    save.mutate(payload)
  }

  return (
    <div className="space-y-6">
      <div><h1 className="text-2xl font-semibold text-slate-900">User management</h1><p className="text-slate-600 mt-1">Manage staff accounts and roles (ADMIN only).</p></div>
      {msg && <div className="rounded-lg border border-slate-200 bg-white px-4 py-3 text-sm">{msg}</div>}
      <form onSubmit={submit} className="rounded-xl border border-slate-200 bg-white p-5 grid gap-3 sm:grid-cols-2">
        <h2 className="sm:col-span-2 font-semibold">{editing ? 'Edit user' : 'Create user'}</h2>
        <input required placeholder="Full name" value={form.fullName} onChange={e=>setForm({...form, fullName:e.target.value})} className="rounded-lg border border-slate-300 px-3 py-2" />
        <input required type="email" placeholder="Email" value={form.email} onChange={e=>setForm({...form, email:e.target.value})} className="rounded-lg border border-slate-300 px-3 py-2" />
        <input type="password" placeholder={editing ? 'Password (leave blank to keep)' : 'Password'} value={form.password} onChange={e=>setForm({...form, password:e.target.value})} className="rounded-lg border border-slate-300 px-3 py-2" required={!editing} />
        <select value={form.role} onChange={e=>setForm({...form, role:e.target.value})} className="rounded-lg border border-slate-300 px-3 py-2"><option value="CATALOGUER">CATALOGUER</option><option value="ADMIN">ADMIN</option></select>
        <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.active} onChange={e=>setForm({...form, active:e.target.checked})}/> Active</label>
        <div className="sm:col-span-2 flex gap-2"><button className="rounded-lg bg-blue-950 px-4 py-2 text-white">{save.isPending?'Saving…':'Save'}</button>{editing && <button type="button" onClick={()=>{setEditing(null);setForm(empty)}} className="rounded-lg border border-slate-300 px-4 py-2">Cancel</button>}</div>
      </form>
      <div className="rounded-xl border border-slate-200 bg-white overflow-auto">
        {q.isLoading ? <div className="h-40 bg-slate-100 animate-pulse"/> : (
          <table className="w-full text-sm"><thead className="bg-slate-50 text-slate-600"><tr><th className="px-3 py-2 text-left">Name</th><th className="px-3 py-2 text-left">Email</th><th className="px-3 py-2 text-left">Role</th><th className="px-3 py-2 text-left">Active</th><th></th></tr></thead>
          <tbody>{(q.data||[]).map(u=> (<tr key={u.id} className="border-t border-slate-100"><td className="px-3 py-2">{u.fullName}</td><td className="px-3 py-2">{u.email}</td><td className="px-3 py-2">{u.role}</td><td className="px-3 py-2">{String(u.active)}</td><td className="px-3 py-2 text-right whitespace-nowrap"><button onClick={()=>{setEditing(u.id); setForm({fullName:u.fullName,email:u.email,password:'',role:u.role,active:u.active})}} className="text-blue-900 hover:underline mr-3">Edit</button><button onClick={()=>{if(confirm('Delete user?')) del.mutate(u.id)}} className="text-red-700 hover:underline">Delete</button></td></tr>))}
          {(q.data||[]).length===0 && <tr><td colSpan={5} className="px-3 py-8 text-center text-slate-500">No users.</td></tr>}
          </tbody></table>
        )}
      </div>
    </div>
  )
}
