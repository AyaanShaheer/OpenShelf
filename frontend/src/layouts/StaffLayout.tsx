import { Link, NavLink, Outlet } from 'react-router-dom'
import { LayoutDashboard, Library, Upload, RefreshCw, Users, LogOut, BookCopy } from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'

function NavItem({ to, icon: Icon, label }: { to: string; icon: any; label: string }) {
  return (
    <NavLink to={to} end={to === '/staff'} className={({isActive}) => `flex items-center gap-2 rounded-lg px-3 py-2 text-sm ${isActive ? 'bg-blue-950 text-white' : 'text-slate-700 hover:bg-slate-100'}`}>
      <Icon size={16} /> {label}
    </NavLink>
  )
}

export function StaffLayout() {
  const { user, logout, isAdmin } = useAuth()
  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link to="/staff" className="flex items-center gap-2 font-semibold"><span className="inline-flex h-9 w-9 items-center justify-center rounded-lg bg-blue-950 text-white"><Library size={18} /></span> OpenShelf Staff</Link>
          <div className="flex items-center gap-4 text-sm">
            <span className="text-slate-600">{user?.fullName} <span className="text-slate-400">({user?.role})</span></span>
            <button onClick={logout} className="inline-flex items-center gap-1.5 text-slate-600 hover:text-slate-900"><LogOut size={16}/> Sign out</button>
          </div>
        </div>
      </header>
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-6 grid gap-6 lg:grid-cols-[240px_1fr]">
        <aside className="rounded-xl border border-slate-200 bg-white p-3 h-fit space-y-1">
          <NavItem to="/staff" icon={LayoutDashboard} label="Overview" />
          <NavItem to="/staff/resources" icon={BookCopy} label="Catalogue" />
          <NavItem to="/staff/imports" icon={Upload} label="XML Imports" />
          <NavItem to="/staff/indexing" icon={RefreshCw} label="Indexing" />
          {isAdmin && <NavItem to="/staff/users" icon={Users} label="Users" />}
          <div className="pt-2 mt-2 border-t border-slate-100"><Link to="/" className="block px-3 py-2 text-sm text-slate-600 hover:text-slate-900">Public portal</Link></div>
        </aside>
        <section className="min-w-0"><Outlet /></section>
      </div>
    </div>
  )
}
