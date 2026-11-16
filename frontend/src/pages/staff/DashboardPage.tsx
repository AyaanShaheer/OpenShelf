import { useQuery } from '@tanstack/react-query'
import { api } from '@/api/client'
import type { DashboardStats } from '@/types/api'

function Stat({ label, value }: { label: string; value: number | string }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="text-sm text-slate-500">{label}</div>
      <div className="mt-1 text-2xl font-semibold text-slate-900">{value}</div>
    </div>
  )
}

export function DashboardPage() {
  const q = useQuery({ queryKey: ['dashboard'], queryFn: () => api.get<DashboardStats>('/admin/dashboard') })
  const health = useQuery({ queryKey: ['system-health'], queryFn: () => api.get<{ status: string; application: string; profiles: string[] }>('/admin/system/health') })
  if (q.isLoading) return <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">{Array.from({length:6}).map((_,i)=><div key={i} className="h-24 rounded-xl bg-slate-200 animate-pulse"/>)}</div>
  if (q.isError || !q.data) return <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-red-700">Unable to load dashboard statistics.</div>
  const d = q.data
  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900">Administrative overview</h1>
      <p className="text-slate-600 mt-1">Live catalogue and operations statistics.</p>
      <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Stat label="Total resources" value={d.totalResources} />
        <Stat label="Active resources" value={d.activeResources} />
        <Stat label="Archived resources" value={d.archivedResources} />
        <Stat label="Indexed" value={d.indexedResources} />
        <Stat label="Pending indexing" value={d.pendingIndexing} />
        <Stat label="Indexing failures" value={d.failedIndexing} />
        <Stat label="Import jobs" value={d.importJobs} />
      </div>
      <div className="mt-6 rounded-xl border border-slate-200 bg-white p-5">
        <h2 className="font-semibold text-slate-900">Application health</h2>
        <div className="mt-3 flex flex-wrap items-center gap-4 text-sm">
          <span className={`inline-flex items-center gap-2 ${health.data?.status === 'UP' ? 'text-green-700' : 'text-red-700'}`}>
            <span className={`h-2 w-2 rounded-full ${health.data?.status === 'UP' ? 'bg-green-600' : 'bg-red-600'}`} />
            {health.isLoading ? 'Checking…' : health.data?.status || 'Unavailable'}
          </span>
          {health.data && <span className="text-slate-600">{health.data.application} · profiles: {health.data.profiles?.join(', ') || 'default'}</span>}
        </div>
      </div>
    </div>
  )
}
