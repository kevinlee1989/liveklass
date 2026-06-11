import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/creators': 'http://localhost:8080',
      '/courses': 'http://localhost:8080',
      '/sale-records': 'http://localhost:8080',
      '/settlements': 'http://localhost:8080',
      '/cancellation-records': 'http://localhost:8080',
    },
  },
})
