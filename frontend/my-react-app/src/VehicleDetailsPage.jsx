import { useEffect, useState } from 'react';
import { API_BASE } from './apiConfig.js';

// currency formatter from your CatalogPage
const money = new Intl.NumberFormat('en-CA', {
  style: 'currency',
  currency: 'CAD',
});

const number = new Intl.NumberFormat('en-CA');

export default function VehicleDetailsPage({ vehicleId, onBack, onLogin }) {
  
  const [result, setResult] = useState({
    loading: true,
    vehicle: null,
    error: '',
  });

  useEffect(() => {
    const controller = new AbortController();
    let active = true;

    if (!vehicleId) {
      setResult({ loading: false, vehicle: null, error: 'Add ?id=<vehicle id> to the URL.' });
      return;
    }

    setResult({ loading: true, vehicle: null, error: '' });

    fetch(`${API_BASE}/vehicles/${encodeURIComponent(vehicleId)}`, { signal: controller.signal })
      .then(async (response) => {
        const data = await response.json().catch(() => null);
        
        if (!response.ok) {
          if (response.status === 404) throw new Error('Vehicle not found.');
          throw new Error(`Error ${response.status}: ${data?.message || ''}`);
        }
        return data;
      })
      .then((vehicle) => {
        if (active) {
          setResult({ loading: false, vehicle, error: '' });
        }
      })
      .catch((error) => {
        if (active && error.name !== 'AbortError') {
          // Handle network errors gracefully like the original script
          const errorMsg = error.message.includes('Error') || error.message.includes('not found') 
            ? error.message 
            : 'Could not reach the server.';
          setResult({ loading: false, vehicle: null, error: errorMsg });
        }
      });

    return () => {
      active = false;
      controller.abort();
    };
  }, [vehicleId]);

  const handleLogout = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('Not logged in.');
      return;
    }
    try {
      await fetch(`${API_BASE}/auth/logout`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
    } catch (err) {
      // ignore network errors, still forget the token locally
    }
    localStorage.removeItem('token');
    alert('Logged out.');
    onBack();
  };

  const { loading, vehicle, error } = result;

  return (
    <main className="wrap section">
      <h1>Vehicle details</h1>

      {loading && <p role="status">Loading...</p>}
      {error && <p role="alert" style={{ color: 'red' }}>{error}</p>}

      {vehicle && (
        <>
          <table border="1" style={{ borderCollapse: 'collapse', marginBottom: '1rem' }}>
            <tbody>
              {['make', 'model', 'year', 'bodyStyle'].map((key) => (
                <tr key={key}>
                  <td style={{ padding: '0.5rem', fontWeight: 'bold', textTransform: 'capitalize' }}>
                    {key === 'bodyStyle' ? 'Body style' : key}
                  </td>
                  <td style={{ padding: '0.5rem' }}>
                    {vehicle[key] || 'N/A'}
                  </td>
                </tr>
              ))}
              <tr>
                <td style={{ padding: '0.5rem', fontWeight: 'bold' }}>Price</td>
                <td style={{ padding: '0.5rem' }}>{money.format(vehicle.price)}</td>
              </tr>
              <tr>
                <td style={{ padding: '0.5rem', fontWeight: 'bold' }}>Mileage</td>
                <td style={{ padding: '0.5rem' }}>{number.format(vehicle.mileage)} km</td>
              </tr>
            </tbody>
          </table>

          <h2>Vehicle history report</h2>
          <p>{vehicle.historyReport || 'No history report on file.'}</p>
        </>
      )}

      <div style={{ marginTop: '2rem', display: 'flex', gap: '1rem', alignItems: 'center' }}>
        <button type="button" onClick={handleLogout} className="btn btn-light">
          Logout
        </button>
        <button type="button" onClick={onLogin} className="text-link">
          Login
        </button>
        <button type="button" onClick={onBack} className="text-link">
          Go Back
        </button>
      </div>
    </main>
  );
}
