import { useCallback, useEffect, useState } from 'react'
import './pages.css'
import { getCart, updateCartItem, removeCartItem } from './cartApi.js'

// Formats a number as Canadian dollars with cents, e.g. $48,990.00.
const money = (amount) =>
  Number(amount).toLocaleString('en-CA', {
    style: 'currency',
    currency: 'CAD',
  })

/**
 * Shopping cart page.
 *   user         logged-in user ({ userId, ... }) or null
 *   onBack       return to the home page
 *   onSignIn     go to the login page (shown when signed out)
 *   onCartChange called with the item count so the navbar badge stays in sync
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

  // Stores a fresh cart and tells the parent the new count for the badge.
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

  // Picks which view to show: signed out / loading / empty / list of items.
  const hasItems = cart.items.length > 0

  return (
    <main className="page-shell">
      <section className="cart-panel">
        <button className="back-link" onClick={onBack} type="button">
          ← Continue browsing
        </button>

        <p className="eyebrow">YOUR SELECTION</p>
        <h1>Shopping cart</h1>
        <p className="muted">
          Review your selected electric vehicles before checkout.
        </p>

        {error && (
          <p className="form-error" role="alert">
            {error}
          </p>
        )}

        <div className="static-cart-layout">
          {!user ? (
            <div className="empty-cart">
              <div className="empty-cart-icon">⌑</div>
              <h2>Sign in to see your cart</h2>
              <p className="muted">
                Your cart is saved to your account. Sign in to add and manage
                vehicles.
              </p>
              <button className="primary-button" type="button" onClick={onSignIn}>
                Sign in
              </button>
            </div>
          ) : loading ? (
            <p className="muted">Loading your cart…</p>
          ) : !hasItems ? (
            <div className="empty-cart">
              <div className="empty-cart-icon">⌑</div>
              <h2>Your cart is waiting</h2>
              <p className="muted">
                You haven't added any vehicles yet. Explore our electric
                vehicle collection to find your next ride.
              </p>
              <button className="primary-button" type="button" onClick={onBack}>
                Browse vehicles
              </button>
            </div>
          ) : (
            <ul className="cart-lines">
              {cart.items.map((item) => {
                // Disable this row's controls while its request is in flight.
                const busy = busyItemId === item.itemId
                const name = `${item.year} ${item.make} ${item.model}`
                return (
                  <li className="cart-line" key={item.itemId} aria-busy={busy}>
                    {/* Show the photo if the vehicle has one, otherwise a placeholder box */}
                    {item.imageUrl ? (
                      <img className="cart-line-image" src={item.imageUrl} alt={name} />
                    ) : (
                      <div className="cart-line-image cart-line-placeholder">⌑</div>
                    )}

                    <div className="cart-line-info">
                      <h3>{name}</h3>
                      <p className="muted">{item.bodyStyle || 'Electric vehicle'}</p>
                      <p className="cart-line-price">{money(item.price)} each</p>

                      <div className="cart-line-controls">
                        {/* Quantity stepper: − is disabled at 1 (use Remove to delete), + at the 99 cap */}
                        <div className="qty-stepper" role="group" aria-label={`Quantity for ${name}`}>
                          <button
                            type="button"
                            aria-label="Decrease quantity"
                            disabled={busy || item.quantity <= 1}
                            onClick={() => changeQuantity(item, item.quantity - 1)}
                          >
                            −
                          </button>
                          <span aria-live="polite">{item.quantity}</span>
                          <button
                            type="button"
                            aria-label="Increase quantity"
                            disabled={busy || item.quantity >= 99}
                            onClick={() => changeQuantity(item, item.quantity + 1)}
                          >
                            +
                          </button>
                        </div>

                        <button
                          className="remove-button"
                          type="button"
                          disabled={busy}
                          onClick={() => remove(item)}
                        >
                          Remove
                        </button>
                      </div>
                    </div>

                    <strong className="cart-line-total">{money(item.lineTotal)}</strong>
                  </li>
                )
              })}
            </ul>
          )}

          {/* Totals come from the server response, never calculated in the browser */}
          <aside className="order-summary">
            <h2>Order summary</h2>
            <div className="summary-row">
              <span>Items</span>
              <strong>{cart.itemCount}</strong>
            </div>
            <div className="summary-row">
              <span>Subtotal</span>
              <strong>{money(cart.subtotal)}</strong>
            </div>
            <div className="summary-row">
              <span>Taxes</span>
              <span>Calculated later</span>
            </div>
            <div className="summary-row total-row">
              <span>Total</span>
              <strong>{money(cart.subtotal)}</strong>
            </div>
            {/* Checkout is out of scope for these tickets */}
            <button className="primary-button" type="button" disabled>
              Proceed to checkout
            </button>
            <p className="muted small-text">
              Checkout is not available in this preview.
            </p>
          </aside>
        </div>
      </section>
    </main>
  )
}
