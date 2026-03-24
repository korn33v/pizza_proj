import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dev-proxy: browser calls /orders -> Vite proxies to backend.
// PROXY_TARGET is read only by the dev server (NOT exposed to the browser).
const target = process.env.PROXY_TARGET || 'http://localhost:8080'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/orders': {
        target,
        changeOrigin: true
      }
    }
  }
})
