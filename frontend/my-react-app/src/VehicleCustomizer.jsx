
import { useMemo, useState } from 'react'

const OPTIONS = {
  exterior: [
    { value: 'Midnight Black', price: 0 },
    { value: 'Ocean Blue', price: 300 },
    { value: 'Pearl White', price: 500 },
    { value: 'Racing Red', price: 400 },
  ],
  interior: [
    { value: 'Light', price: 0 },
    { value: 'Dark', price: 0 },
    { value: 'Sport', price: 600 },
  ],
  wheels: [
    { value: 'Standard', price: 0 },
    { value: 'Sport', price: 800 },
    { value: 'Premium', price: 1200 },
  ],
  accessories: [
    { value: 'Floor mats', price: 100 },
    { value: 'Cargo organizer', price: 150 },
    { value: 'Roof rack', price: 350 },
  ],
  packages: [
    { value: 'Comfort', price: 500 },
    { value: 'Technology', price: 900 },
    { value: 'Sport', price: 1000 },
  ],
}

const INITIAL_CONFIGURATION = {
  exterior: 'Midnight Black',
  interior: 'Light',
  wheels: 'Standard',
  accessories: [],
  packages: [],
}

const money = (amount) =>
  Number(amount).toLocaleString('en-CA', {
    style: 'currency',
    currency: 'CAD',
  })

export default function VehicleCustomizer({
  vehicle,
  onAddToCart,
}) {
  const [configuration, setConfiguration] = useState(
    INITIAL_CONFIGURATION
  )
  const [message, setMessage] = useState('')
  const [adding, setAdding] = useState(false)

  const optionCost = useMemo(() => {
    const price = (group, value) =>
      OPTIONS[group].find((option) => option.value === value)?.price || 0

    return (
      price('exterior', configuration.exterior) +
      price('interior', configuration.interior) +
      price('wheels', configuration.wheels) +
      configuration.accessories.reduce(
        (total, value) => total + price('accessories', value), 0
      ) +
      configuration.packages.reduce(
        (total, value) => total + price('packages', value), 0
      )
    )
  }, [configuration])

  const total = Number(vehicle.price || 0) + optionCost

  function updateSingle(group, value) {
    setConfiguration((current) => ({
      ...current,
      [group]: value,
    }))
    setMessage('')
  }

  function updateMultiple(group, value, checked) {
    setConfiguration((current) => ({
      ...current,
      [group]: checked
        ? [...current[group], value]
        : current[group].filter((item) => item !== value),
    }))
    setMessage('')
  }

  async function handleAddToCart() {
    if (!onAddToCart) {
      setMessage('Cart functionality is unavailable.')
      return
    }

    setAdding(true)
    setMessage('')

    try {
      const result = await onAddToCart(vehicle, configuration)

      if (result === 'login-required') {
        setMessage('Please log in to add this vehicle.')
      } else if (result === false) {
        setMessage('Could not add vehicle to cart.')
      } else {
        setMessage('Vehicle added to cart.')
      }
    } catch (error) {
      setMessage(error.message || 'Could not add vehicle to cart.')
    } finally {
      setAdding(false)
    }
  }

  function singleChoice(group, title) {
    return (
      <fieldset>
        <legend>{title}</legend>
        {OPTIONS[group].map((option) => (
          <label key={option.value}>
            <input
              type="radio"
              name={group}
              checked={configuration[group] === option.value}
              onChange={() => updateSingle(group, option.value)}
            />
            {option.value} (+{money(option.price)})
          </label>
        ))}
      </fieldset>
    )
  }

  function multipleChoice(group, title) {
    return (
      <fieldset>
        <legend>{title}</legend>
        {OPTIONS[group].map((option) => (
          <label key={option.value}>
            <input
              type="checkbox"
              checked={configuration[group].includes(option.value)}
              onChange={(event) =>
                updateMultiple(group, option.value, event.target.checked)
              }
            />
            {option.value} (+{money(option.price)})
          </label>
        ))}
      </fieldset>
    )
  }

  return (
    <section>
      <h2>Customize this vehicle</h2>

      {singleChoice('exterior', 'Exterior colour')}
      {singleChoice('interior', 'Interior')}
      {singleChoice('wheels', 'Wheels')}
      {multipleChoice('accessories', 'Accessories')}
      {multipleChoice('packages', 'Packages')}

      <h3>Price summary</h3>
      <p>Base price: {money(vehicle.price || 0)}</p>
      <p>Customization cost: {money(optionCost)}</p>
      <strong>Total: {money(total)}</strong>

      <p>
        <button
          type="button"
          onClick={handleAddToCart}
          disabled={adding}
        >
          {adding ? 'Adding...' : 'Add configured vehicle to cart'}
        </button>
      </p>

      {message && <p role="status">{message}</p>}
    </section>
  )
}
