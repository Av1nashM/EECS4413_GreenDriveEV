// Thin wrapper around the cart REST endpoints (PB-15 add, PB-16 edit, PB-17 remove).
// Every call is scoped to a user via the ?userId= query parameter, matching CartController.
// Base URL of the Spring Boot backend.
export const API_BASE = 'http://localhost:8080/api'

// Shared fetch helper: sends/receives JSON and turns any failure into an Error
// whose message is safe to show to the user.
async function request(path, options = {}) {
  let response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...options.headers },
    })
  } catch {
    // fetch only rejects on network problems (backend down, CORS, offline).
    throw new Error('Could not reach the server. Make sure the backend is running.')
  }

  // Some responses may have no JSON body, so don't let parsing throw.
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    // The backend reports problems as { error: "..." }; fall back to the status code.
    throw new Error(
      body?.error || body?.message || `Request failed (${response.status})`
    )
  }
  return body
}

// GET the cart: { items, itemCount, subtotal }.
export const getCart = (userId) => request(`/cart?userId=${userId}`)

// PB-15: add a vehicle. Adding one that is already in the cart increases its quantity.
export const addToCart = (userId, vehicleId, quantity = 1) =>
  request(`/cart/items?userId=${userId}`, {
    method: 'POST',
    body: JSON.stringify({ vehicleId, quantity }),
  })

// PB-16: set a cart line to an exact quantity.
export const updateCartItem = (userId, itemId, quantity) =>
  request(`/cart/items/${itemId}?userId=${userId}`, {
    method: 'PUT',
    body: JSON.stringify({ quantity }),
  })

// PB-17: delete a cart line.
export const removeCartItem = (userId, itemId) =>
  request(`/cart/items/${itemId}?userId=${userId}`, { method: 'DELETE' })

// Checks credentials; on success resolves to { userId, name, email }.
export const login = (email, password) =>
  request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })