// Em: vite.config.js

/// <reference types="vitest" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom', // ✅ GARANTA QUE ESTA LINHA EXISTA E ESTEJA CORRETA
    setupFiles: './src/setupTests.js',
    css: true,
  },
})