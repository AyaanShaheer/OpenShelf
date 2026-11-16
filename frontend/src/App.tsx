import { Navigate, Route, Routes } from 'react-router-dom'
import { PublicLayout } from '@/layouts/PublicLayout'
import { StaffLayout } from '@/layouts/StaffLayout'
import { ProtectedRoute } from '@/components/ProtectedRoute'
import { HomePage } from '@/pages/public/HomePage'
import { SearchPage } from '@/pages/public/SearchPage'
import { ResourceDetailPage } from '@/pages/public/ResourceDetailPage'
import { LoginPage } from '@/pages/public/LoginPage'
import { DashboardPage } from '@/pages/staff/DashboardPage'
import { ResourcesPage } from '@/pages/staff/ResourcesPage'
import { ResourceFormPage } from '@/pages/staff/ResourceFormPage'
import { ImportsPage } from '@/pages/staff/ImportsPage'
import { ImportDetailPage } from '@/pages/staff/ImportDetailPage'
import { IndexingPage } from '@/pages/staff/IndexingPage'
import { UsersPage } from '@/pages/staff/UsersPage'

export default function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<HomePage />} />
        <Route path="search" element={<SearchPage />} />
        <Route path="resources/:id" element={<ResourceDetailPage />} />
        <Route path="login" element={<LoginPage />} />
      </Route>

      <Route path="staff" element={<ProtectedRoute><StaffLayout /></ProtectedRoute>}>
        <Route index element={<DashboardPage />} />
        <Route path="resources" element={<ResourcesPage />} />
        <Route path="resources/new" element={<ResourceFormPage />} />
        <Route path="resources/:id/edit" element={<ResourceFormPage />} />
        <Route path="imports" element={<ImportsPage />} />
        <Route path="imports/:id" element={<ImportDetailPage />} />
        <Route path="indexing" element={<IndexingPage />} />
        <Route path="users" element={<ProtectedRoute adminOnly><UsersPage /></ProtectedRoute>} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
