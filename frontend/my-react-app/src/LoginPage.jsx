import { useState } from 'react'
import { login } from './cartAPI.js'

/**
 * Sign-in page. On success it calls onLoggedIn(user) with { userId, name, email }
 * so the app can remember who is logged in (needed to load their cart).
 */
export default function LoginPage({ onBack, onLoggedIn, onGoToRegister }) {
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
    <main>
      <p>
        <button type="button" onClick={onBack}>
          Back to home
        </button>
      </p>

      <h1>Sign in</h1>

      <form onSubmit={handleSubmit}>
        <p>
          <label htmlFor="email">Email </label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </p>

        <p>
          <label htmlFor="password">Password </label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </p>

        {error && <p role="alert">{error}</p>}

        <p>
          <button type="submit" disabled={submitting}>
            {submitting ? 'Signing in...' : 'Sign in'}
          </button>
        </p>
      </form>

      <p>
        Don't have an account?{' '}
        <button type="button" onClick={onGoToRegister}>
          Create account
        </button>
      </p>
    </main>
  )
}
