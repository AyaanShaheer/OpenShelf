import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import { api, ApiError } from '@/api/client'
import type { Resource } from '@/types/api'

const schema = z.object({
  title: z.string().min(1, 'Title is required'),
  description: z.string().optional(),
  publisher: z.string().optional(),
  publicationDate: z.string().optional(),
  resourceType: z.string().min(1, 'Resource type is required'),
  language: z.string().optional(),
  isbnOrIdentifier: z.string().optional(),
  format: z.string().optional(),
  sourceUrl: z.string().url('Must be a valid URL').optional().or(z.literal('')),
  rights: z.string().optional(),
  authors: z.string().optional(),
  subjects: z.string().optional(),
})
type Form = z.infer<typeof schema>

const types = ['BOOK','RESEARCH_PAPER','JOURNAL','THESIS','ARTICLE','OTHER']

export function ResourceFormPage() {
  const { id } = useParams()
  const isEdit = !!id
  const nav = useNavigate()
  const qc = useQueryClient()
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<Form>({ resolver: zodResolver(schema), defaultValues: { resourceType: 'BOOK' } })

  const detail = useQuery({ queryKey: ['admin-resource', id], queryFn: () => api.get<Resource>(`/admin/resources/${id}`), enabled: isEdit })
  useEffect(() => {
    if (detail.data) {
      const r = detail.data
      reset({
        title: r.title || '', description: r.description || '', publisher: r.publisher || '',
        publicationDate: r.publicationDate || '', resourceType: r.resourceType || 'BOOK', language: r.language || '',
        isbnOrIdentifier: r.isbnOrIdentifier || '', format: r.format || '', sourceUrl: r.sourceUrl || '', rights: r.rights || '',
        authors: (r.authors||[]).join(', '), subjects: (r.subjects||[]).join(', '),
      })
    }
  }, [detail.data, reset])

  const mutation = useMutation({
    mutationFn: (payload: any) => isEdit ? api.put(`/admin/resources/${id}`, payload) : api.post('/admin/resources', payload),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['admin-resources'] }); qc.invalidateQueries({ queryKey: ['dashboard'] }); nav('/staff/resources') },
  })

  const onSubmit = async (data: Form) => {
    const payload = {
      ...data,
      publicationDate: data.publicationDate || null,
      sourceUrl: data.sourceUrl || null,
      description: data.description || null,
      publisher: data.publisher || null,
      language: data.language || null,
      isbnOrIdentifier: data.isbnOrIdentifier || null,
      format: data.format || null,
      rights: data.rights || null,
      authors: (data.authors||'').split(',').map(s=>s.trim()).filter(Boolean),
      subjects: (data.subjects||'').split(',').map(s=>s.trim()).filter(Boolean),
    }
    try { await mutation.mutateAsync(payload) } catch (e: any) { alert(e instanceof ApiError ? e.message : 'Save failed') }
  }

  if (isEdit && detail.isLoading) return <div className="h-64 rounded-xl bg-slate-200 animate-pulse"/>
  return (
    <div className="max-w-3xl">
      <h1 className="text-2xl font-semibold text-slate-900">{isEdit ? 'Edit resource' : 'Create resource'}</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4 rounded-xl border border-slate-200 bg-white p-6">
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2"><label htmlFor="title" className="block text-sm font-medium mb-1">Title *</label><input id="title" {...register('title')} className="w-full rounded-lg border border-slate-300 px-3 py-2"/>{errors.title && <p className="text-sm text-red-600 mt-1">{errors.title.message}</p>}</div>
          <div className="sm:col-span-2"><label htmlFor="description" className="block text-sm font-medium mb-1">Description</label><textarea id="description" rows={4} {...register('description')} className="w-full rounded-lg border border-slate-300 px-3 py-2"/></div>
          <div><label htmlFor="resourceType" className="block text-sm font-medium mb-1">Resource type *</label><select id="resourceType" {...register('resourceType')} className="w-full rounded-lg border border-slate-300 px-3 py-2">{types.map(t=> <option key={t} value={t}>{t}</option>)}</select></div>
          <div><label htmlFor="publicationDate" className="block text-sm font-medium mb-1">Publication date</label><input id="publicationDate" type="date" {...register('publicationDate')} className="w-full rounded-lg border border-slate-300 px-3 py-2"/></div>
          <div><label htmlFor="publisher" className="block text-sm font-medium mb-1">Publisher</label><input id="publisher" {...register('publisher')} className="w-full rounded-lg border border-slate-300 px-3 py-2"/></div>
          <div><label htmlFor="language" className="block text-sm font-medium mb-1">Language</label><input id="language" {...register('language')} className="w-full rounded-lg border border-slate-300 px-3 py-2"/></div>
          <div><label htmlFor="isbnOrIdentifier" className="block text-sm font-medium mb-1">Identifier (ISBN/DOI)</label><input id="isbnOrIdentifier" {...register('isbnOrIdentifier')} className="w-full rounded-lg border border-slate-300 px-3 py-2"/></div>
          <div><label htmlFor="format" className="block text-sm font-medium mb-1">Format</label><input id="format" {...register('format')} className="w-full rounded-lg border border-slate-300 px-3 py-2"/></div>
          <div className="sm:col-span-2"><label htmlFor="sourceUrl" className="block text-sm font-medium mb-1">Source URL</label><input id="sourceUrl" {...register('sourceUrl')} className="w-full rounded-lg border border-slate-300 px-3 py-2"/>{errors.sourceUrl && <p className="text-sm text-red-600 mt-1">{errors.sourceUrl.message}</p>}</div>
          <div className="sm:col-span-2"><label htmlFor="rights" className="block text-sm font-medium mb-1">Rights</label><input id="rights" {...register('rights')} className="w-full rounded-lg border border-slate-300 px-3 py-2"/></div>
          <div className="sm:col-span-2"><label htmlFor="authors" className="block text-sm font-medium mb-1">Authors (comma-separated)</label><input id="authors" {...register('authors')} className="w-full rounded-lg border border-slate-300 px-3 py-2"/></div>
          <div className="sm:col-span-2"><label htmlFor="subjects" className="block text-sm font-medium mb-1">Subjects (comma-separated)</label><input id="subjects" {...register('subjects')} className="w-full rounded-lg border border-slate-300 px-3 py-2"/></div>
        </div>
        <div className="flex gap-3 pt-2">
          <button disabled={isSubmitting || mutation.isPending} className="rounded-lg bg-blue-950 px-4 py-2 text-white hover:bg-blue-900 disabled:opacity-60">{isSubmitting||mutation.isPending ? 'Saving…' : 'Save resource'}</button>
          <button type="button" onClick={()=>nav('/staff/resources')} className="rounded-lg border border-slate-300 px-4 py-2">Cancel</button>
        </div>
      </form>
    </div>
  )
}
