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
    } catch {
      setStatus({
        loading: false,
        message: 'Could not reach the server.',
        isError: true,
      });
    }
  };

  return (
    <main>
      <p>
        <button type="button" onClick={onBack}>
          Back to home
        </button>
      </p>

      <h1>Register</h1>

      <form onSubmit={handleSubmit}>
        <p>
          <label htmlFor="name">Name </label>
          <input
            id="name"
            type="text"
            required
            maxLength="100"
            value={formData.name}
            onChange={handleChange}
          />
        </p>

        <p>
          <label htmlFor="email">Email </label>
          <input
            id="email"
            type="email"
            required
            value={formData.email}
            onChange={handleChange}
          />
        </p>

        <p>
          <label htmlFor="password">Password (min 8) </label>
          <input
            id="password"
            type="password"
            required
            minLength="8"
            value={formData.password}
            onChange={handleChange}
          />
        </p>

        <p>
          <button type="submit" disabled={status.loading}>
            {status.loading ? 'Registering...' : 'Register'}
          </button>
        </p>
      </form>

      {status.message && (
        <p role={status.isError ? 'alert' : 'status'}>{status.message}</p>
      )}

      <p>
        Already have an account?{' '}
        <button type="button" onClick={onGoToLogin}>
          Login
        </button>
      </p>
    </main>
  );
}
