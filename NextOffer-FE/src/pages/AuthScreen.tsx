import { useState } from 'react'
import { formatAuthError, useAuth } from '../context/AuthContext'
import { PanelHeader } from '../components/PanelHeader'
import '../App.css'

export function AuthScreen() {
  const { login, register } = useAuth()
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login')
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      if (authMode === 'register') {
        await register(email, password, fullName)
      } else {
        await login(email, password)
      }
    } catch (err) {
      setError(formatAuthError(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="auth-shell">
      <article className="panel auth-panel auth-panel-centered">
        <PanelHeader title="NextOffer" action={authMode === 'login' ? 'Sign in' : 'Register'} />
        <p className="auth-lead">Sign in to sync company watches and discovered jobs from the backend.</p>

        <div className="segmented-control">
          <button
            className={authMode === 'login' ? 'active' : ''}
            onClick={() => setAuthMode('login')}
            type="button"
          >
            Login
          </button>
          <button
            className={authMode === 'register' ? 'active' : ''}
            onClick={() => setAuthMode('register')}
            type="button"
          >
            Register
          </button>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          {authMode === 'register' && (
            <label>
              Full name
              <input
                placeholder="Alex Morgan"
                value={fullName}
                onChange={(event) => setFullName(event.target.value)}
                required
              />
            </label>
          )}
          <label>
            Email
            <input
              placeholder="alex@university.edu"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>
          <label>
            Password
            <input
              placeholder="Minimum 8 characters"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              minLength={8}
              required
            />
          </label>
          {error && <p className="form-error">{error}</p>}
          <button className="primary-button" disabled={submitting} type="submit">
            {submitting ? 'Please wait…' : authMode === 'login' ? 'Sign in' : 'Create account'}
          </button>
        </form>
      </article>
    </main>
  )
}
