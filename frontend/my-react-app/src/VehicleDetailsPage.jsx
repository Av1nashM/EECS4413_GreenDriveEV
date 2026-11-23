import { useEffect, useState } from 'react';
import { API_BASE } from './apiConfig.js';
import VehicleCustomizer from './VehicleCustomizer.jsx';

const money = new Intl.NumberFormat('en-CA', {
  style: 'currency',
  currency: 'CAD',
});

const number = new Intl.NumberFormat('en-CA');

export default function VehicleDetailsPage({ vehicleId, onBack, onLogin, onAddToCart }) {
  const [result, setResult] = useState({
    loading: true,
    vehicle: null,
    error: '',
  });

  useEffect(() => {
    const controller = new AbortController();
    let active = true;

    if (!vehicleId) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setResult({ loading: false, vehicle: null, error: 'No vehicle selected.' });
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
          // Show server errors as-is; anything else means we could not connect.
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
    } catch {
      // ignore network errors, still forget the token locally
    }
    localStorage.removeItem('token');
    alert('Logged out.');
    onBack();
  };

  const { loading, vehicle, error } = result;

  return (
    <main>
      <p>
        <button type="button" onClick={onBack}>
          Back to home
        </button>{' '}
        <button type="button" onClick={onLogin}>
          Login
        </button>{' '}
        <button type="button" onClick={handleLogout}>
          Logout
        </button>
      </p>

      <h1>Vehicle details</h1>

      {loading && <p role="status">Loading...</p>}
      {error && <p role="alert">{error}</p>}

      {vehicle && (
        <>
          <table border="1" cellPadding="6">
            <tbody>
              <tr>
                <th scope="row">Make</th>
                <td>{vehicle.make || 'N/A'}</td>
              </tr>
              <tr>
                <th scope="row">Model</th>
                <td>{vehicle.model || 'N/A'}</td>
              </tr>
              <tr>
                <th scope="row">Year</th>
                <td>{vehicle.year || 'N/A'}</td>
              </tr>
              <tr>
                <th scope="row">Body style</th>
                <td>{vehicle.bodyStyle || 'N/A'}</td>
              </tr>
              <tr>
                <th scope="row">Price</th>
                <td>{money.format(vehicle.price)}</td>
              </tr>
              <tr>
                <th scope="row">Mileage</th>
                <td>{number.format(vehicle.mileage)} km</td>
              </tr>
            </tbody>
          </table>

          <h2>Vehicle history report</h2>
          <p>{vehicle.historyReport || 'No history report on file.'}</p>
          <VehicleCustomizer vehicle={vehicle} onAddToCart={onAddToCart} />
        </>
      )}
    </main>
  );
}
