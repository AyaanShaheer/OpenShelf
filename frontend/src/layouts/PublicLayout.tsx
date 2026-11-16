import { Link, NavLink, Outlet } from 'react-router-dom'
import { BookOpen, Library, LogIn } from 'lucide-react'

export function PublicLayout() {
  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <header className="border-b border-slate-200/80 bg-white/90 backdrop-blur sticky top-0 z-30">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 font-semibold text-slate-900">
            <span className="inline-flex h-9 w-9 items-center justify-center rounded-lg bg-blue-950 text-white"><Library size={18} /></span>
            <span className="tracking-tight">OpenShelf</span>
          </Link>
          <nav className="flex items-center gap-6 text-sm">
            <NavLink to="/" className={({isActive}) => isActive ? 'text-blue-950 font-medium' : 'text-slate-600 hover:text-slate-900'}>Home</NavLink>
            <NavLink to="/search" className={({isActive}) => isActive ? 'text-blue-950 font-medium' : 'text-slate-600 hover:text-slate-900'}>Browse Catalogue</NavLink>
            <Link to="/login" className="inline-flex items-center gap-2 rounded-lg bg-blue-950 px-3.5 py-2 text-white hover:bg-blue-900 transition-colors">
              <LogIn size={16} /> Staff Sign In
            </Link>
          </nav>
        </div>
      </header>
      <main><Outlet /></main>
      <footer className="mt-16 border-t border-slate-200 bg-slate-950 text-slate-300">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-10 grid gap-6 md:grid-cols-3">
          <div>
            <div className="flex items-center gap-2 text-white font-semibold"><BookOpen size={18} /> OpenShelf</div>
            <p className="mt-3 text-sm text-slate-400">Digital library discovery and catalogue operations for academic and public institutions.</p>
          </div>
          <div>
            <h3 className="text-sm font-semibold text-white">Explore</h3>
            <ul className="mt-3 space-y-2 text-sm">
              <li><Link to="/search" className="hover:text-white">Search catalogue</Link></li>
              <li><Link to="/search?types=BOOK" className="hover:text-white">Books</Link></li>
              <li><Link to="/search?types=JOURNAL" className="hover:text-white">Journals</Link></li>
            </ul>
          </div>
          <div>
            <h3 className="text-sm font-semibold text-white">Access</h3>
            <p className="mt-3 text-sm text-slate-400">Public discovery is open. Staff administration requires authorized sign-in.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
