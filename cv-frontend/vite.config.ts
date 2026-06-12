import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  base: '/team-theta-ten-thirty/',
  plugins: [react()],
  server: {
    port: 5173
  }
});
