export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string | null
  timestamp?: string
}

export interface ApiErrorBody {
  timestamp?: string
  status?: number
  error?: string
  code?: string
  message?: string
  path?: string
  violations?: { field: string; message: string }[]
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface UserSummary {
  id: number
  fullName: string
  email: string
  role: 'ADMIN' | 'CATALOGUER'
  active?: boolean
}

export interface AuthResponse {
  token: string
  tokenType: string
  expiresInMs: number
  user: UserSummary
}

export interface Resource {
  id: number
  title: string
  description?: string | null
  publisher?: string | null
  publicationDate?: string | null
  resourceType?: string | null
  language?: string | null
  isbnOrIdentifier?: string | null
  format?: string | null
  sourceUrl?: string | null
  rights?: string | null
  status?: string | null
  authors?: string[]
  subjects?: string[]
  createdAt?: string
  updatedAt?: string
}

export interface SearchResponse {
  results: Resource[]
  total: number
  page: number
  size: number
  facets: Record<string, { value: string; count: number }[]>
}

export interface DashboardStats {
  totalResources: number
  activeResources: number
  archivedResources: number
  indexedResources: number
  pendingIndexing: number
  failedIndexing: number
  importJobs: number
}

export interface ImportPreviewRecord {
  identifier?: string | null
  title?: string | null
  creator?: string | null
  subject?: string | null
  description?: string | null
  publisher?: string | null
  date?: string | null
  type?: string | null
  format?: string | null
  source?: string | null
  language?: string | null
  rights?: string | null
  errors: string[]
  duplicate: boolean
}

export interface ImportPreviewResponse {
  importJobId: number
  filename: string
  totalRecords: number
  validRecords: number
  invalidRecords: number
  duplicateRecords: number
  records: ImportPreviewRecord[]
}

export interface ImportJob {
  id: number
  filename: string
  status: string
  totalRecords: number
  successfulRecords: number
  failedRecords: number
  duplicateRecords: number
  importedBy?: string | null
  startedAt?: string | null
  completedAt?: string | null
  errors?: { recordIdentifier?: string | null; errorMessage: string }[]
}
