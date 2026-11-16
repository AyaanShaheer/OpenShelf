import type { ApiErrorBody, ApiResponse } from '@/types/api'

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

export class ApiError extends Error {
  status: number
  body?: ApiErrorBody
  constructor(message: string, status: number, body?: ApiErrorBody) {
    super(message)
    this.status = status
    this.body = body
  }
}

export function getToken() {
  return localStorage.getItem('openshelf_token')
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem('openshelf_token', token)
  else localStorage.removeItem('openshelf_token')
}

export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = getToken()
  const headers = new Headers(init.headers)
  if (!headers.has('Content-Type') && !(init.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const res = await fetch(`${API_BASE}${path}`, { ...init, headers })
  const text = await res.text()
  let json: any = null
  try { json = text ? JSON.parse(text) : null } catch { json = null }

  if (!res.ok) {
    const body = json as ApiErrorBody | undefined
    const msg = body?.message || body?.error || `Request failed (${res.status})`
    throw new ApiError(msg, res.status, body)
  }
  if (json && typeof json === 'object' && 'success' in json && 'data' in json) {
    return (json as ApiResponse<T>).data
  }
  return json as T
}

export const api = {
  get: <T>(path: string) => apiFetch<T>(path),
  post: <T>(path: string, body?: unknown) => apiFetch<T>(path, { method: 'POST', body: body instanceof FormData ? body : JSON.stringify(body) }),
  put: <T>(path: string, body?: unknown) => apiFetch<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  del: <T>(path: string) => apiFetch<T>(path, { method: 'DELETE' }),
}
