import { useEffect, useState } from 'react'
import { getVehicles, getVehicleDetails } from './catalogAPI.js'

const money = new Intl.NumberFormat('en-CA', {
  style: 'currency',
  currency: 'CAD',
})

const number = new Intl.NumberFormat('en-CA')

const accidentLabels = {
  NO_REPORTED_ACCIDENTS: 'No reported accidents',
  ACCIDENT_REPORTED: 'Accident reported',
  UNKNOWN: 'Unknown',
}

function VehicleDetails({ vehicleId, onClose }) {
  const [result, setResult] = useState({
    loading: true,
    vehicle: null,
    error: '',
  })

  useEffect(() => {
    const controller = new AbortController()
    let active = true

    getVehicleDetails(vehicleId, controller.signal)
      .then((vehicle) => {
        if (active) {
          setResult({ loading: false, vehicle, error: '' })
        }
      })
      .catch((error) => {
        if (active && error.name !== 'AbortError') {
          setResult({
            loading: false,
            vehicle: null,
            error: error.message,
          })
        }
      })

    return () => {
      active = false
      controller.abort()
    }
  }, [vehicleId])

  const vehicle = result.vehicle

  return (
    <section aria-labelledby="vehicle-details-heading">
      <h2 id="vehicle-details-heading">Vehicle details</h2>

      <button type="button" onClick={onClose}>
        Close details
      </button>

      {result.loading && <p role="status">Loading details...</p>}
      {result.error && <p role="alert">{result.error}</p>}

      {vehicle && (
        <>
          <h3>
            {vehicle.year} {vehicle.make} {vehicle.model}
          </h3>

          <dl>
            <dt>Body style</dt>
            <dd>{vehicle.bodyStyle || 'Not available'}</dd>

            <dt>Price</dt>
            <dd>{money.format(vehicle.price)}</dd>

            <dt>Mileage</dt>
            <dd>{number.format(vehicle.mileage)} km</dd>
          </dl>

          <h4>Vehicle history report</h4>
          <p>{vehicle.historyReport || 'No history report on file.'}</p>
        </>
      )}
    </section>
  )
}

export default function CatalogPage({
  onBack,
  onViewCart,
  onAddToCart,
  addingVehicleId,
  cartNotice,
}) {
  const [query, setQuery] = useState({})
  const [selectedId, setSelectedId] = useState(null)
  const [result, setResult] = useState({
    loading: true,
    vehicles: [],
    error: '',
  })

  useEffect(() => {
    const controller = new AbortController()
    let active = true

    getVehicles(query, controller.signal)
      .then((vehicles) => {
        if (active) {
          setResult({ loading: false, vehicles, error: '' })
        }
      })
      .catch((error) => {
        if (active && error.name !== 'AbortError') {
          setResult({
            loading: false,
            vehicles: [],
            error: error.message,
          })
        }
      })

    return () => {
      active = false
      controller.abort()
    }
  }, [query])

  function applyQuery(nextQuery) {
    setSelectedId(null)
    setResult({ loading: true, vehicles: [], error: '' })
    setQuery(nextQuery)
  }

  function handleSubmit(event) {
    event.preventDefault()

    const filters = Object.fromEntries(
      new FormData(event.currentTarget).entries()
    )

    applyQuery(filters)
  }

  return (
    <main className="wrap section">
      <nav aria-label="Catalogue navigation">
        <button type="button" onClick={onBack}>
          Back to home
        </button>
        {' '}
        <button type="button" onClick={onViewCart}>
          View cart
        </button>
      </nav>

      <h1>GreenDriveEV Catalogue</h1>
      <p>Leave filters blank to include all values.</p>

      <form
        onSubmit={handleSubmit}
        onReset={() => applyQuery({})}
        aria-busy={result.loading}
      >
        <fieldset disabled={result.loading}>
          <legend>Filter and sort vehicles</legend>

          <p>
            <label htmlFor="catalog-make">Brand </label>
            <input
              id="catalog-make"
              name="make"
              type="text"
              placeholder="Example: Tesla"
            />
          </p>

          <p>
            <label htmlFor="catalog-body-style">Body style </label>
            <input
              id="catalog-body-style"
              name="bodyStyle"
              type="text"
              placeholder="Example: SUV"
            />
          </p>

          <p>
            <label htmlFor="catalog-year">Model year </label>
            <input
              id="catalog-year"
              name="year"
              type="number"
              min="1"
              max="9999"
              step="1"
            />
          </p>

          <p>
            <label htmlFor="catalog-history">Accident history </label>
            <select
              id="catalog-history"
              name="accidentStatus"
              defaultValue=""
            >
              <option value="">All histories</option>
              <option value="NO_REPORTED_ACCIDENTS">
                No reported accidents
              </option>
              <option value="ACCIDENT_REPORTED">Accident reported</option>
              <option value="UNKNOWN">Unknown</option>
            </select>
          </p>

          <p>
            <label htmlFor="catalog-sort">Sort by </label>
            <select id="catalog-sort" name="sortBy" defaultValue="price">
              <option value="price">Price</option>
              <option value="mileage">Mileage</option>
            </select>

            {' '}
            <label htmlFor="catalog-order">Order </label>
            <select id="catalog-order" name="order" defaultValue="asc">
              <option value="asc">Lowest first</option>
              <option value="desc">Highest first</option>
            </select>
          </p>

          <button type="submit">Apply filters</button>
          {' '}
          <button type="reset">Reset</button>
        </fieldset>
      </form>

      {result.loading && <p role="status">Loading vehicles...</p>}
      {result.error && <p role="alert">{result.error}</p>}

      {!result.loading && !result.error && (
        <>
          <p role="status">
            {result.vehicles.length === 0
              ? 'No vehicles found. Try changing or resetting your filters.'
              : `${result.vehicles.length} vehicle(s) found.`}
          </p>

          {result.vehicles.length > 0 && (
            <div style={{ overflowX: 'auto' }}>
              <table>
                <caption>Catalogue results</caption>
                <thead>
                  <tr>
                    <th scope="col">Vehicle</th>
                    <th scope="col">Body style</th>
                    <th scope="col">Price (CAD)</th>
                    <th scope="col">Mileage (km)</th>
                    <th scope="col">Accident history</th>
                    <th scope="col">Actions</th>
                  </tr>
                </thead>

                <tbody>
                  {result.vehicles.map((vehicle) => (
                    <tr key={vehicle.id}>
                      <td>
                        {vehicle.year} {vehicle.make} {vehicle.model}
                      </td>
                      <td>{vehicle.bodyStyle || 'Not available'}</td>
                      <td>{money.format(vehicle.price)}</td>
                      <td>{number.format(vehicle.mileage)}</td>
                      <td>
                        {accidentLabels[vehicle.accidentStatus] || 'Unknown'}
                      </td>
                      <td>
                        <button
                          type="button"
                          onClick={() => setSelectedId(vehicle.id)}
                        >
                          View details
                        </button>
                        {' '}
                        <button
                          type="button"
                          disabled={addingVehicleId != null}
                          onClick={() => onAddToCart(vehicle)}
                        >
                          {addingVehicleId === vehicle.id
                            ? 'Adding...'
                            : 'Add to cart'}
                        </button>

                        {cartNotice?.vehicleId === vehicle.id && (
                          <p role={cartNotice.isError ? 'alert' : 'status'}>
                            {cartNotice.text}
                          </p>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      {selectedId !== null && (
        <VehicleDetails
          key={selectedId}
          vehicleId={selectedId}
          onClose={() => setSelectedId(null)}
        />
      )}
    </main>
  )
}