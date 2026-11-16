import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { api, setToken, getToken } from '@/api/client'
import type { AuthResponse, UserSummary } from '@/types/api'

interface AuthContextValue {
  user: UserSummary | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  isStaff: boolean
  isAdmin: boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = getToken()
    if (!token) { setLoading(false); return }
    api.get<UserSummary & { active?: boolean }>('/auth/me')
      .then((me) => setUser({ id: me.id, fullName: me.fullName, email: me.email, role: me.role, active: me.active }))
      .catch(() => { setToken(null); setUser(null) })
      .finally(() => setLoading(false))
  }, [])

  const login = async (email: string, password: string) => {
    const res = await api.post<AuthResponse>('/auth/login', { email, password })
    setToken(res.token)
    setUser(res.user)
  }

  const logout = () => {
    setToken(null)
    setUser(null)
  }

  const value = useMemo(() => ({
    user,
    loading,
    login,
    logout,
    isStaff: !!user && (user.role === 'ADMIN' || user.role === 'CATALOGUER'),
    isAdmin: user?.role === 'ADMIN',
  }), [user, loading])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
