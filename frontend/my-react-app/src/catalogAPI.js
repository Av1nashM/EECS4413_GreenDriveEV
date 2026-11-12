import { API_BASE } from './apiConfig.js'

async function request(path, signal) {
  let response

  try {
    response = await fetch(`${API_BASE}/v1/vehicles${path}`, {
      signal,
      headers: { Accept: 'application/json' },
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Could not reach the server. Please try again.')
  }

  const body = await response.json().catch(() => null)

  if (!response.ok) {
    throw new Error(
      body?.message || `Could not load vehicles (${response.status}).`
    )
  }

  if (body === null) {
    throw new Error('The server returned an unexpected response.')
  }

  return body
}

export async function getVehicles(filters = {}, signal) {
  const parameters = new URLSearchParams()

  for (const [name, value] of Object.entries(filters)) {
    const text = String(value ?? '').trim()

    if (text !== '') {
      parameters.set(name, text)
    }
  }

  const query = parameters.toString()
  const vehicles = await request(query ? `?${query}` : '', signal)

  if (!Array.isArray(vehicles)) {
    throw new Error('The server returned an unexpected vehicle list.')
  }

  return vehicles
}

export function getVehicleDetails(vehicleId, signal) {
  return request(`/${encodeURIComponent(vehicleId)}`, signal)
}