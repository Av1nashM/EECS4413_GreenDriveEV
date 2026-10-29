
import './pages.css';

export default function LoginPage({ onBack }) {
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

        <form className="page-form">
          <label htmlFor="email">Email address</label>
          <input
            id="email"
            type="email"
            placeholder="Enter your email"
          />

          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            placeholder="Enter your password"
          />

          <div className="form-options">
            <label className="remember-label">
              <input type="checkbox" />
              Remember me
            </label>
            <span className="text-link">Forgot password?</span>
          </div>

          <button className="primary-button" type="button">
            Sign in
          </button>
        </form>

        <p className="muted small-text">
          Don't have an account? <span className="text-link">Create account</span>
        </p>
      </section>
    </main>
  );
}
