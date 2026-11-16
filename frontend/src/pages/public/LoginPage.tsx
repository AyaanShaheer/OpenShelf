import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { useEffect, useState } from 'react'
import { ApiError } from '@/api/client'

const schema = z.object({ email: z.string().email('Enter a valid email'), password: z.string().min(1, 'Password is required') })
type Form = z.infer<typeof schema>

export function LoginPage() {
  const { login, user } = useAuth()
  const nav = useNavigate()
  const loc = useLocation() as any
  const [error, setError] = useState<string | null>(null)
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<Form>({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (user) nav('/staff', { replace: true })
  }, [user, nav])

  const onSubmit = async (data: Form) => {
    setError(null)
    try {
      await login(data.email, data.password)
      nav(loc.state?.from?.pathname || '/staff', { replace: true })
    } catch (e: any) {
      setError(e instanceof ApiError ? e.message : 'Sign in failed')
    }
  }

  return (
    <div className="min-h-[70vh] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-2xl font-semibold text-slate-900">Staff sign in</h1>
        <p className="mt-1 text-slate-600">Access catalogue administration and operations.</p>
        {error && <div className="mt-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>}
        <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-slate-700 mb-1">Email</label>
            <input id="email" type="email" {...register('email')} className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:ring-2 focus:ring-blue-900/20 focus:border-blue-900" />
            {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>}
          </div>
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-slate-700 mb-1">Password</label>
            <input id="password" type="password" {...register('password')} className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:ring-2 focus:ring-blue-900/20 focus:border-blue-900" />
            {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>}
          </div>
          <button disabled={isSubmitting} className="w-full rounded-lg bg-blue-950 py-2.5 text-white font-medium hover:bg-blue-900 disabled:opacity-60">{isSubmitting ? 'Signing in…' : 'Sign in'}</button>
        </form>
        <p className="mt-4 text-xs text-slate-500">Staff accounts are provisioned by administrators. Public browsing does not require sign-in.</p>
      </div>
    </div>
  )
}
