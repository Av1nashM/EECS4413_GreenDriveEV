import { useState } from 'react'
import './pages.css'
import { login } from './cartApi.js'

/**
 * Sign-in page. On success it calls onLoggedIn(user) with { userId, name, email }
 * so the app can remember who is logged in (needed to load their cart).
 */
export default function LoginPage({ onBack, onLoggedIn }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  // Sends the credentials to the backend and shows any error from it.
  const handleSubmit = async (e) => {
    e.preventDefault() // stop the browser from reloading the page
    setError('')
    setSubmitting(true)
    try {
      const user = await login(email, password)
      onLoggedIn(user)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="page-shell">
      <section className="auth-card">
        <button className="back-link" onClick={onBack} type="button">
          ← Back to home
        </button>

        <div className="page-brand">GreenDrive<span>EV</span></div>

        <p className="eyebrow">WELCOME BACK</p>
        <h1>Sign in</h1>
        <p className="muted">
          Welcome back! Sign in to continue your electric vehicle journey.
        </p>

        <form className="page-form" onSubmit={handleSubmit}>
          <label htmlFor="email">Email address</label>
          <input
            id="email"
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <div className="form-options">
            <label className="remember-label">
              <input type="checkbox" />
              Remember me
            </label>
            <span className="text-link">Forgot password?</span>
          </div>

          {error && (
            <p className="form-error" role="alert">
              {error}
            </p>
          )}

          <button className="primary-button" type="submit" disabled={submitting}>
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <p className="muted small-text">
          Don't have an account? <span className="text-link">Create account</span>
        </p>
      </section>
    </main>
  )
}
