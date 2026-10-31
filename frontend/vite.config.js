import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],

  server: {
    proxy: {
      // /api로 시작하는 요청을 Spring Boot 서버에 전달
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})