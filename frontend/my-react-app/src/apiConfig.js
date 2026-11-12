export const API_BASE = (
  import.meta.env.VITE_API_BASE_URL ||
  'https://eecs4413-greendriveev.onrender.com/api'
).replace(/\/+$/, '')