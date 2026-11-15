
import { useState, useEffect, useMemo } from 'react'
import LoginPage from './LoginPage.jsx';
import CartPage from './CartPage.jsx';
import { addToCart, getCart } from './cartAPI.js';
import CatalogPage from './CatalogPage.jsx'
import { API_BASE } from './apiConfig.js'
import VehicleDetailsPage from './VehicleDetailsPage.jsx';
import RegisterPage from './RegisterPage.jsx';

// localStorage key where the logged-in user is remembered between page reloads.
const USER_KEY = 'greendrive.user'

// Reads the saved user; returns null if nothing is saved or storage is unavailable.
function loadStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY))
  } catch {
    return null
  }
}


// Shuffles an array randomly for the home page
const shuffleArray = (array) => {
  return [...array].sort(() => Math.random() - 0.5);
};

function App() {
  const [price, setPrice] = useState(48990)
  const [downPayment, setDownPayment] = useState(5000)
  const [rate, setRate] = useState(6.99)
  const [term, setTerm] = useState(60)
  const [currentPage, setCurrentPage] = useState('home');
  const [selectedVehicleId, setSelectedVehicleId] = useState(null); 
  const [homeCategory, setHomeCategory] = useState('All');

  // --- Cart / session state ---
  const [user, setUser] = useState(loadStoredUser)
  // Number shown on the navbar cart icon.
  const [cartCount, setCartCount] = useState(0)
  // Message shown under the vehicle card that was just added (or failed to add).
  const [cartNotice, setCartNotice] = useState(null) // { vehicleId, text, isError }
  // Vehicle whose "Add to cart" request is in flight (disables its button).
  const [addingVehicleId, setAddingVehicleId] = useState(null)

    const [vehicles, setVehicles] = useState([])
  const [loadingVehicles, setLoadingVehicles] = useState(true)
  const [vehicleError, setVehicleError] = useState('')

  useEffect(() => {
    const fetchVehicles = async () => {
      try {
        const response = await fetch(`${API_BASE}/vehicles`)

        if (!response.ok) {
          throw new Error(`Server returned ${response.status}`)
        }

        const data = await response.json()
        setVehicles(data)
      } catch (error) {
        setVehicleError(
          'Could not load vehicles. Make sure the backend and database are running.'
        )
        console.error('Error fetching vehicles:', error)
      } finally {
        setLoadingVehicles(false)
      }
    }

    fetchVehicles()
  }, [])

  const userId = user?.userId

  // Keep the navbar badge in sync with the saved cart.
  useEffect(() => {
    if (!userId) return
    getCart(userId)
      .then((cart) => setCartCount(cart.itemCount))
      .catch(() => setCartCount(0))
  }, [userId])

  // Called by the login page: remember the user and return home.
  const handleLoggedIn = (loggedInUser) => {
    try {
      localStorage.setItem(USER_KEY, JSON.stringify(loggedInUser))
    } catch {
      // storage unavailable; session just won't persist across reloads
    }
    setUser(loggedInUser)
    setCurrentPage('home')
  }

  const handleLogout = () => {
    try {
      localStorage.removeItem(USER_KEY);
    } catch {
      // ignore storage errors
    }
    setUser(null);
    setCartCount(0);
    setCurrentPage('home');
  }

  // PB-15: add one of this vehicle; signed-out visitors are sent to the login page first.
  const handleAddToCart = async (vehicle) => {
    if (!userId) {
      setCurrentPage('login')
      return
    }
    setAddingVehicleId(vehicle.id)
    setCartNotice(null)
    try {
      await addToCart(userId, vehicle.id, 1)
      // Re-read the cart so the badge reflects the server's count.
      const cart = await getCart(userId)
      setCartCount(cart.itemCount)
      setCartNotice({ vehicleId: vehicle.id, text: 'Added to cart ✓' })
    } catch (error) {
      setCartNotice({ vehicleId: vehicle.id, text: error.message, isError: true })
    } finally {
      setAddingVehicleId(null)
    }
  }

  // Three random vehicles in the chosen category. Memoised so the list does not
  // reshuffle every time something else on the page (e.g. the calculator) changes.
  const featuredVehicles = useMemo(
    () =>
      shuffleArray(
        vehicles.filter((v) => homeCategory === 'All' || v.bodyStyle === homeCategory)
      ).slice(0, 3),
    [vehicles, homeCategory]
  )

  const principal = Math.max(0, Number(price) - Number(downPayment))
  const monthlyRate = Number(rate) / 100 / 12

  const payment =
    Number(term) > 0
      ? monthlyRate === 0
        ? principal / Number(term)
        : (principal * monthlyRate) /
          (1 - Math.pow(1 + monthlyRate, -Number(term)))
      : 0

  const money = (amount) =>
    amount.toLocaleString('en-CA', {
      style: 'currency',
      currency: 'CAD',
      maximumFractionDigits: 0,
    })

  if (currentPage === 'login') {
    return (
      <LoginPage
        onBack={() => setCurrentPage('home')}
        onLoggedIn={handleLoggedIn}
        onGoToRegister={() => setCurrentPage('register')}
      />
    );
  }

  if (currentPage === 'register') {
    return (
      <RegisterPage
        onBack={() => setCurrentPage('home')}
        onGoToLogin={() => setCurrentPage('login')}
      />
    );
  }

  if (currentPage === 'cart') {
    return (
      <CartPage
        user={user}
        onBack={() => setCurrentPage('home')}
        onSignIn={() => setCurrentPage('login')}
        onCartChange={setCartCount}
      />
    );
  }

  if (currentPage === 'catalog') {
    return (
      <CatalogPage
        onBack={() => setCurrentPage('home')}
        onViewCart={() => setCurrentPage('cart')}
        onAddToCart={handleAddToCart}
        addingVehicleId={addingVehicleId}
        cartNotice={cartNotice}
      />
    )
  }

    if (currentPage === 'vehicleDetails') {
    return (
      <VehicleDetailsPage
        vehicleId={selectedVehicleId}
        onBack={() => setCurrentPage('home')}
        onLogin={() => setCurrentPage('login')}
      />
    );
  }

  return (
    <>
      <header>
        <h1>GreenDriveEV</h1>
        <p>Find your next electric drive.</p>

        <nav>
          <button type="button" onClick={() => setCurrentPage('catalog')}>
            Shop EVs
          </button>{' '}
          <button type="button" onClick={() => setCurrentPage('cart')}>
            Cart ({userId ? cartCount : 0})
          </button>{' '}
          {userId ? (
            <button type="button" onClick={handleLogout}>
              Sign out
            </button>
          ) : (
            <>
              <button type="button" onClick={() => setCurrentPage('login')}>
                Sign in
              </button>{' '}
              <button type="button" onClick={() => setCurrentPage('register')}>
                Register
              </button>
            </>
          )}
        </nav>
        <hr />
      </header>

      <main>
        <section>
          <h2>Featured vehicles</h2>

          <p>
            <label htmlFor="homeCategory">Body style </label>
            <select
              id="homeCategory"
              value={homeCategory}
              onChange={(e) => setHomeCategory(e.target.value)}
            >
              {['All', 'SUV', 'Sedan', 'Hatchback', 'Truck'].map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>{' '}
            <button type="button" onClick={() => setCurrentPage('catalog')}>
              View all vehicles
            </button>
          </p>

          {loadingVehicles ? (
            <p>Loading vehicles...</p>
          ) : vehicleError ? (
            <p role="alert">{vehicleError}</p>
          ) : featuredVehicles.length === 0 ? (
            <p>No vehicles found.</p>
          ) : (
            <ul>
              {featuredVehicles.map((vehicle) => (
                <li key={vehicle.id}>
                  <h3>
                    {vehicle.year} {vehicle.make} {vehicle.model}
                  </h3>
                  <p>
                    {vehicle.bodyStyle || 'Electric vehicle'}
                    <br />
                    Price: {money(vehicle.price)}
                    <br />
                    Mileage: {Number(vehicle.mileage).toLocaleString('en-CA')} km
                  </p>

                  <button
                    type="button"
                    onClick={() => {
                      setSelectedVehicleId(vehicle.id)
                      setCurrentPage('vehicleDetails')
                    }}
                  >
                    View details
                  </button>{' '}
                  {/* PB-15: add this vehicle to the cart */}
                  <button
                    type="button"
                    disabled={addingVehicleId === vehicle.id}
                    onClick={() => handleAddToCart(vehicle)}
                  >
                    {addingVehicleId === vehicle.id ? 'Adding...' : 'Add to cart'}
                  </button>

                  {cartNotice?.vehicleId === vehicle.id && (
                    <p role={cartNotice.isError ? 'alert' : 'status'}>
                      {cartNotice.text}
                    </p>
                  )}
                </li>
              ))}
            </ul>
          )}
        </section>

        <hr />

        <section>
          <h2>Monthly payment calculator</h2>
          <p>Adjust the numbers to estimate a monthly payment.</p>

          <p>
            <label htmlFor="calcPrice">Vehicle price (CAD) </label>
            <input
              id="calcPrice"
              type="number"
              min="0"
              value={price}
              onChange={(e) => setPrice(Math.max(0, Number(e.target.value)))}
            />
          </p>

          <p>
            <label htmlFor="calcDown">Down payment (CAD) </label>
            <input
              id="calcDown"
              type="number"
              min="0"
              value={downPayment}
              onChange={(e) => setDownPayment(Math.max(0, Number(e.target.value)))}
            />
          </p>

          <p>
            <label htmlFor="calcRate">Interest rate (%) </label>
            <input
              id="calcRate"
              type="number"
              min="0"
              step="0.1"
              value={rate}
              onChange={(e) => setRate(Math.max(0, Number(e.target.value)))}
            />
          </p>

          <p>
            <label htmlFor="calcTerm">Loan term </label>
            <select
              id="calcTerm"
              value={term}
              onChange={(e) => setTerm(Number(e.target.value))}
            >
              <option value="36">36 months</option>
              <option value="48">48 months</option>
              <option value="60">60 months</option>
              <option value="72">72 months</option>
              <option value="84">84 months</option>
            </select>
          </p>

          <p>
            Estimated monthly payment: <strong>{money(payment)}</strong> CAD / month
            (before taxes and fees)
          </p>
          <p>
            <small>Illustrative estimate only. Actual financing terms may vary.</small>
          </p>
        </section>
      </main>

      <footer>
        <hr />
        <p>© 2026 GreenDriveEV. EECS4413-GroupC</p>
      </footer>
    </>
  )
}

export default App
