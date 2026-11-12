import { useState } from 'react';
import { API_BASE } from './apiConfig.js';

export default function RegisterPage({ onBack, onGoToLogin }) {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
  });
  
  const [status, setStatus] = useState({
    loading: false,
    message: '',
    isError: false,
  });

  const handleChange = (e) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatus({ loading: true, message: '', isError: false });

    try {
      const response = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });

      const data = await response.json().catch(() => null);

      if (response.ok) {
        setStatus({
          loading: false,
          message: `Registered as ${data.email}. You can now log in.`,
          isError: false,
        });
      } else {
        setStatus({
          loading: false,
          message: `Error ${response.status}: ${data && data.message ? data.message : 'request failed'}`,
          isError: true,
        });
      }
    } catch (err) {
      setStatus({
        loading: false,
        message: 'Could not reach the server.',
        isError: true,
      });
    }
  };

  return (
    <main className="wrap section" style={{ maxWidth: '400px', margin: '0 auto', paddingTop: '2rem' }}>
      <nav aria-label="Navigation" style={{ marginBottom: '1rem' }}>
        <button type="button" onClick={onBack} className="btn btn-light">
          ← Back to Home
        </button>
      </nav>

      <h1>Register</h1>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1.5rem' }}>
        <div>
          <label htmlFor="name" style={{ display: 'block', marginBottom: '0.5rem' }}>Name:</label>
          <input
            id="name"
            type="text"
            required
            maxLength="100"
            value={formData.name}
            onChange={handleChange}
            className="calc-input"
            style={{ width: '100%', padding: '0.5rem' }}
          />
        </div>

        <div>
          <label htmlFor="email" style={{ display: 'block', marginBottom: '0.5rem' }}>Email:</label>
          <input
            id="email"
            type="email"
            required
            value={formData.email}
            onChange={handleChange}
            className="calc-input"
            style={{ width: '100%', padding: '0.5rem' }}
          />
        </div>

        <div>
          <label htmlFor="password" style={{ display: 'block', marginBottom: '0.5rem' }}>Password (min 8):</label>
          <input
            id="password"
            type="password"
            required
            minLength="8"
            value={formData.password}
            onChange={handleChange}
            className="calc-input"
            style={{ width: '100%', padding: '0.5rem' }}
          />
        </div>

        <button type="submit" className="btn btn-primary" disabled={status.loading}>
          {status.loading ? 'Registering...' : 'Register'}
        </button>
      </form>

      {status.message && (
        <p 
          role={status.isError ? 'alert' : 'status'} 
          style={{ 
            marginTop: '1rem', 
            color: status.isError ? '#dc2626' : '#16a34a',
            fontWeight: 'bold'
          }}
        >
          {status.message}
        </p>
      )}

      <p style={{ marginTop: '2rem' }}>
        <button 
          type="button" 
          onClick={onGoToLogin} 
          className="text-link" 
          style={{ background: 'none', border: 'none', cursor: 'pointer', textDecoration: 'underline', padding: 0 }}
        >
          Already have an account? Login
        </button>
      </p>
    </main>
  );
}