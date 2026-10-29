
import { useState, useEffect } from 'react'
function App() {
  const [searchText, setSearchText] = useState('')
  const [make, setMake] = useState('')
  const [bodyStyle, setBodyStyle] = useState('')
  const [budget, setBudget] = useState('')
  const [price, setPrice] = useState(48990)
  const [downPayment, setDownPayment] = useState(5000)
  const [rate, setRate] = useState(6.99)
  const [term, setTerm] = useState(60)

    const [vehicles, setVehicles] = useState([])
  const [loadingVehicles, setLoadingVehicles] = useState(true)
  const [vehicleError, setVehicleError] = useState('')

  useEffect(() => {
    const fetchVehicles = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/vehicles')

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

  return (
    <>
      <header className="navbar">
        <div className="wrap navinner">
          <div className="brand">
            <span>GreenDrive<em>EV</em></span>
          </div>

          <nav className="navlinks">
            <span className="navlink">Shop EVs</span>
            <span className="navlink">Why GreenDrive</span>
            <span className="navlink">Finance</span>
            <span className="navlink">Reviews</span>
          </nav>

          <div className="navactions">
            <button className="iconbtn" aria-label="Shopping cart">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.8"
              >
                <path d="M3 4h2l2.2 11.2a2 2 0 0 0 2 1.6h8.9a2 2 0 0 0 1.9-1.4L22 9H6" />
                <circle cx="10" cy="20" r="1" />
                <circle cx="18" cy="20" r="1" />
              </svg>
              <span className="count">0</span>
            </button>

            <button className="btn btn-primary">
              Explore inventory
            </button>

            <button className="signin-btn">
              Sign in
            </button>
          </div>
        </div>
      </header>

      <main id="home">
        <section className="hero">
          <div className="wrap">
            <div className="hero-panel">
              <div className="hero-copy">
                <div className="eyebrow">The electric difference</div>

                <h1>
                  Find your next <span>electric</span> drive.
                </h1>

                <p>
                  Great EVs, honest information, and a buying experience designed
                  around you. Make your next move a smarter one.
                </p>

                <div className="hero-ctas">
                  <button className="btn btn-primary">
                    Explore our EVs <span>↗</span>
                  </button>

                  <button className="btn btn-light">
                    Why go electric?
                  </button>
                </div>

                <div className="hero-proof">
                  <div className="avatars">
                    <i>JM</i>
                    <i>AK</i>
                    <i>SL</i>
                    <i>+</i>
                  </div>

                  <span>
                    <b>1,200+ happy drivers</b>
                    <br />
                    making the switch with confidence
                  </span>
                </div>
              </div>

              <div className="hero-visual"></div>
            </div>

            <div className="searchbar" aria-label="Find a vehicle">
              <div className="search-field">
                <label htmlFor="searchText">Find your EV</label>
                <input
                  id="searchText"
                  value={searchText}
                  onChange={(e) => setSearchText(e.target.value)}
                  placeholder="Try Tesla Model Y, Hyundai..."
                />
              </div>

              <div className="search-field">
                <label htmlFor="makeFilter">Make</label>
                <select
                  id="makeFilter"
                  value={make}
                  onChange={(e) => setMake(e.target.value)}
                >
                  <option value="">All makes</option>
                  <option>Tesla</option>
                  <option>Hyundai</option>
                  <option>Kia</option>
                  <option>Polestar</option>
                  <option>Ford</option>
                </select>
              </div>

              <div className="search-field">
                <label htmlFor="typeFilter">Body style</label>
                <select
                  id="typeFilter"
                  value={bodyStyle}
                  onChange={(e) => setBodyStyle(e.target.value)}
                >
                  <option value="">All styles</option>
                  <option>SUV</option>
                  <option>Sedan</option>
                  <option>Hatchback</option>
                </select>
              </div>

              <div className="search-field">
                <label htmlFor="priceFilter">Max. budget</label>
                <select
                  id="priceFilter"
                  value={budget}
                  onChange={(e) => setBudget(e.target.value)}
                >
                  <option value="">Any price</option>
                  <option value="40000">$40,000</option>
                  <option value="50000">$50,000</option>
                  <option value="60000">$60,000</option>
                  <option value="70000">$70,000</option>
                </select>
              </div>

              <button className="btn btn-primary">
                Find my EV <span>→</span>
              </button>
            </div>
          </div>
        </section>

        <section className="section" id="inventory">
          <div className="wrap">
            <div className="section-head">
              <div>
                <div className="eyebrow">
                  Your next chapter starts here
                </div>
                <h2>Explore our electric lineup</h2>
                <p>
                  Handpicked electric vehicles for every kind of journey.
                </p>
              </div>

              <span className="text-link">
                View all vehicles <span>→</span>
              </span>
            </div>

            <div className="filterrow">
              <button className="chip active" data-chip="">
                All vehicles
              </button>
              <button className="chip" data-chip="SUV">
                Electric SUVs
              </button>
              <button className="chip" data-chip="Sedan">
                Sedans
              </button>
              <button className="chip" data-chip="Pre-owned">
                Pre-owned
              </button>
              <button className="chip" data-chip="New arrival">
                New arrivals
              </button>
            </div>

            <div className="vehicle-grid" id="vehicleGrid">
              {loadingVehicles ? (
                <p>Loading vehicles...</p>
              ) : vehicleError ? (
                <p role="alert">{vehicleError}</p>
              ) : vehicles.length === 0 ? (
                <p>No vehicles found in the database.</p>
              ) : (
                vehicles.map((vehicle) => (
                  <article className="vehicle-card" key={vehicle.id}>
                    {vehicle.imageUrl && (
                      <img
                        src={vehicle.imageUrl}
                        alt={`${vehicle.year} ${vehicle.make} ${vehicle.model}`}
                        className="vehicle-image"
                      />
                    )}

                    <div className="vehicle-card-content">
                      <h3>
                        {vehicle.year} {vehicle.make} {vehicle.model}
                      </h3>

                      <p>{vehicle.bodyStyle || 'Electric vehicle'}</p>

                      <strong>{money(vehicle.price)}</strong>

                      <p>
                        Mileage: {Number(vehicle.mileage).toLocaleString('en-CA')} km
                      </p>

                      {vehicle.history && <p>{vehicle.history}</p>}
                    </div>
                  </article>
                ))
              )}
            </div>
          </div>
        </section>

        <section className="section" id="calculator">
          <div className="wrap">
            <div className="feature-panel">
              <div>
                <div className="eyebrow">
                  Make the numbers make sense
                </div>

                <h2>
                  Your EV should fit your life — and your budget.
                </h2>

                <p>
                  Get a quick idea of what your monthly payment could look like.
                  Adjust the numbers to explore an estimate that feels comfortable.
                </p>

                <div className="feature-list">
                  <div><span className="check">✓</span> Flexible down payment</div>
                  <div><span className="check">✓</span> Choose your term</div>
                  <div><span className="check">✓</span> Clear monthly estimate</div>
                  <div><span className="check">✓</span> No commitment</div>
                </div>

                <small style={{ fontSize: '10px', color: '#7a887f' }}>
                  Illustrative estimate only. Actual financing terms may vary.
                </small>
              </div>

              <div className="calculator">
                <h3>Monthly payment calculator</h3>
                <p>Personalize your estimate in seconds.</p>

                <div className="calc-row">
                  <div>
                    <label className="calc-label" htmlFor="calcPrice">
                      Vehicle price (CAD)
                    </label>
                    <input
                      className="calc-input"
                      id="calcPrice"
                      type="number"
                      min="0"
                      value={price}
                      onChange={(e) =>
                        setPrice(Math.max(0, Number(e.target.value)))
                      }
                    />
                  </div>

                  <div>
                    <label className="calc-label" htmlFor="calcDown">
                      Down payment (CAD)
                    </label>
                    <input
                      className="calc-input"
                      id="calcDown"
                      type="number"
                      min="0"
                      value={downPayment}
                      onChange={(e) =>
                        setDownPayment(Math.max(0, Number(e.target.value)))
                      }
                    />
                  </div>

                  <div>
                    <label className="calc-label" htmlFor="calcRate">
                      Interest rate (%)
                    </label>
                    <input
                      className="calc-input"
                      id="calcRate"
                      type="number"
                      min="0"
                      step="0.1"
                      value={rate}
                      onChange={(e) =>
                        setRate(Math.max(0, Number(e.target.value)))
                      }
                    />
                  </div>

                  <div>
                    <label className="calc-label" htmlFor="calcTerm">
                      Loan term
                    </label>
                    <select
                      className="calc-input"
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
                  </div>
                </div>

                <div className="monthly">
                  <div>
                    <span>Estimated monthly payment</span>
                    <strong id="monthlyAmount">{money(payment)}</strong>
                  </div>
                  <small>
                    CAD / month
                    <br />
                    before taxes &amp; fees
                  </small>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="section" id="reviews">
          <div className="wrap">
            <div className="section-head">
              <div>
                <div className="eyebrow">Good drives. Great stories.</div>
                <h2>Made for the way you move.</h2>
                <p>
                  Buying an EV should feel exciting, not overwhelming.
                </p>
              </div>

              <span className="text-link">
                Find your match <span>→</span>
              </span>
            </div>

            <div className="review-grid">
              <div className="testimonial">
                <div className="stars">★★★★★</div>
                <blockquote>
                  “The team actually explained range and charging in a way that
                  made sense for my commute. I felt confident, not pressured.”
                </blockquote>
                <div className="person">
                  <div className="person-avatar">JM</div>
                  <div>
                    <strong>Jordan M.</strong>
                    <small>Hyundai IONIQ 5 owner</small>
                  </div>
                </div>
              </div>

              <div className="testimonial">
                <div className="stars">★★★★★</div>
                <blockquote>
                  “I could compare a few options side by side and understand what
                  I was paying for. The whole process felt refreshingly simple.”
                </blockquote>
                <div className="person">
                  <div className="person-avatar">SK</div>
                  <div>
                    <strong>Sarah K.</strong>
                    <small>Tesla Model 3 owner</small>
                  </div>
                </div>
              </div>

              <div className="testimonial">
                <div className="stars">★★★★★</div>
                <blockquote>
                  “I was shopping for my first EV and had a lot of questions.
                  I got straightforward answers and found a car that fits my family.”
                </blockquote>
                <div className="person">
                  <div className="person-avatar">AL</div>
                  <div>
                    <strong>Alex L.</strong>
                    <small>Kia EV6 owner</small>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>

      <footer className="footer">
        <div className="wrap">
          <div className="footer-grid">
            <div>
              <div className="brand">
                <span>GreenDrive<em>EV</em></span>
              </div>
              <p>
                A more thoughtful way to shop electric. Discover your next
                vehicle with clear information, helpful tools, and a team that
                puts you first.
              </p>
            </div>

            <div>
              <h4>Explore</h4>
              <span>Shop all EVs</span>
              <span>Financing calculator</span>
              <span>Why GreenDrive</span>
            </div>

            <div>
              <h4>Helpful links</h4>
              <span>Compare vehicles</span>
              <span>Payment estimates</span>
              <span>Customer stories</span>
            </div>

            <div>
              <h4>Get in touch</h4>
              <span>hello@greendriveev.ca</span>
              <span>Toronto, Ontario</span>
              <span>Mon–Sat, 9am–6pm</span>
            </div>
          </div>

          <div className="copyright">
            <span>© 2026 GreenDriveEV. EECS4413-GroupC</span>
            <span>Drive thoughtfully. Live electrically.</span>
          </div>
        </div>
      </footer>
    </>
  )
}

export default App
