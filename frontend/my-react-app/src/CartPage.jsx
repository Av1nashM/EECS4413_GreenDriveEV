import { useCallback, useEffect, useState } from 'react'
import { getCart, updateCartItem, removeCartItem } from './cartAPI.js'

// Formats a number as Canadian dollars with cents, e.g. $48,990.00.
const money = (amount) =>
  Number(amount).toLocaleString('en-CA', {
    style: 'currency',
    currency: 'CAD',
  })

/**
 * Shopping cart page (PB-16 edit quantity, PB-17 remove).
 *   user         logged-in user ({ userId, ... }) or null
 *   onBack       return to the home page
 *   onSignIn     go to the login page (shown when signed out)
 *   onCartChange called with the item count so the cart count in the menu stays in sync
 */
export default function CartPage({
  user,
  onBack,
  onSignIn,
  onCartChange,
}) {
  // Cart data exactly as returned by the server.
  const [cart, setCart] = useState({ items: [], itemCount: 0, subtotal: 0 })
  // Only show "loading" if there is a user whose cart we will fetch.
  const [loading, setLoading] = useState(Boolean(user))
  const [error, setError] = useState('')
  // Id of the line currently being updated/removed, so we can disable just that row.
  const [busyItemId, setBusyItemId] = useState(null)

  const userId = user?.userId

  // Stores a fresh cart and tells the parent the new count.
  const applyCart = useCallback(
    (data) => {
      setCart(data)
      onCartChange?.(data.itemCount)
    },
    [onCartChange]
  )

  // Load the cart when the page opens (or the user changes).
  useEffect(() => {
    if (!userId) return
    // Guards against setting state after the page has been left.
    let cancelled = false
    getCart(userId)
      .then((data) => {
        if (!cancelled) applyCart(data)
      })
      .catch((e) => {
        if (!cancelled) setError(e.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [userId, applyCart])

  // Run a mutation on one item, then re-read the cart so totals always come from the server.
  const mutate = async (itemId, action) => {
    setBusyItemId(itemId)
    setError('')
    try {
      await action()
      applyCart(await getCart(userId))
    } catch (e) {
      setError(e.message)
    } finally {
      setBusyItemId(null)
    }
  }

  // PB-16: set a line to a new quantity.
  const changeQuantity = (item, quantity) =>
    mutate(item.itemId, () => updateCartItem(userId, item.itemId, quantity))

  // PB-17: delete a line.
  const remove = (item) =>
    mutate(item.itemId, () => removeCartItem(userId, item.itemId))

  return (
    <main>
      <p>
        <button type="button" onClick={onBack}>
          Continue browsing
        </button>
      </p>

      <h1>Shopping cart</h1>

      {error && <p role="alert">{error}</p>}

      {!user ? (
        <>
          <p>Please sign in to see your cart.</p>
          <button type="button" onClick={onSignIn}>
            Sign in
          </button>
        </>
      ) : loading ? (
        <p>Loading your cart...</p>
      ) : cart.items.length === 0 ? (
        <>
          <p>Your cart is empty.</p>
          <button type="button" onClick={onBack}>
            Browse vehicles
          </button>
        </>
      ) : (
        <>
          <table border="1" cellPadding="6">
            <thead>
              <tr>
                <th>Vehicle</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Total</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {cart.items.map((item) => {
                // Disable this row's buttons while its request is in flight.
                const busy = busyItemId === item.itemId
                const name = `${item.year} ${item.make} ${item.model}`
                return (
                  <tr key={item.itemId}>
                    <td>
                      {name}
                      <br />
                      {item.bodyStyle || 'Electric vehicle'}
                    </td>
                    <td>{money(item.price)}</td>
                    <td>
                      {/* "-" is disabled at 1 (use Remove to delete), "+" at the 99 cap */}
                      <button
                        type="button"
                        aria-label={`Decrease quantity of ${name}`}
                        disabled={busy || item.quantity <= 1}
                        onClick={() => changeQuantity(item, item.quantity - 1)}
                      >
                        -
                      </button>{' '}
                      {item.quantity}{' '}
                      <button
                        type="button"
                        aria-label={`Increase quantity of ${name}`}
                        disabled={busy || item.quantity >= 99}
                        onClick={() => changeQuantity(item, item.quantity + 1)}
                      >
                        +
                      </button>
                    </td>
                    <td>{money(item.lineTotal)}</td>
                    <td>
                      <button
                        type="button"
                        disabled={busy}
                        onClick={() => remove(item)}
                      >
                        Remove
                      </button>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>

          {/* Totals come from the server response, never calculated in the browser */}
          <h2>Order summary</h2>
          <p>Items: {cart.itemCount}</p>
          <p>
            Subtotal: <strong>{money(cart.subtotal)}</strong> (taxes calculated later)
          </p>
          <button type="button" disabled>
            Proceed to checkout
          </button>
          <p>
            <small>Checkout is not available yet.</small>
          </p>
        </>
      )}
    </main>
  )
}
