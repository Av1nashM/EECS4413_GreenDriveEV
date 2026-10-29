
import './pages.css';

export default function CartPage({ onBack }) {
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

        <div className="static-cart-layout">
          <div className="empty-cart">
            <div className="empty-cart-icon">⌑</div>
            <h2>Your cart is waiting</h2>
            <p className="muted">
              You haven't added any vehicles yet. Explore our electric vehicle
              collection to find your next ride.
            </p>
            <button className="primary-button" type="button">
              Browse vehicles
            </button>
          </div>

          <aside className="order-summary">
            <h2>Order summary</h2>
            <div className="summary-row">
              <span>Subtotal</span>
              <strong>$0.00</strong>
            </div>
            <div className="summary-row">
              <span>Taxes</span>
              <span>Calculated later</span>
            </div>
            <div className="summary-row total-row">
              <span>Total</span>
              <strong>$0.00</strong>
            </div>
            <button className="primary-button" type="button">
              Proceed to checkout
            </button>
            <p className="muted small-text">
              Checkout is not available in this preview.
            </p>
          </aside>
        </div>
      </section>
    </main>
  );
}
