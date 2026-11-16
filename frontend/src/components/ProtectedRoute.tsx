import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'

export function ProtectedRoute({ children, adminOnly = false }: { children: React.ReactNode; adminOnly?: boolean }) {
  const { user, loading, isAdmin } = useAuth()
  const location = useLocation()
  if (loading) {
    return <div className="min-h-screen flex items-center justify-center text-slate-600">Loading…</div>
  }
  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }
  if (adminOnly && !isAdmin) {
    return <Navigate to="/staff" replace />
  }
  return <>{children}</>
}
