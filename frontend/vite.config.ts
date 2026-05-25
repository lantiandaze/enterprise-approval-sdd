import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    // host: true 让 dev server 同时绑定 IPv4 (0.0.0.0) 和 IPv6 (::)，
    // 避免某些 Windows / Linux 上 localhost 只解析到 ::1 时浏览器/curl 连不上 127.0.0.1。
    // 如需限制为本机访问，可改为 '127.0.0.1' 或 '::1'。
    host: true,
    port: 5173,
    proxy: {
      '/api': {
        // 目标显式 127.0.0.1，避免 Node 在 Windows 上把 localhost 解析到 ::1 而引发代理失败
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    chunkSizeWarningLimit: 800,
    rollupOptions: {
      output: {
        manualChunks: (id) => {
          if (!id.includes('node_modules')) {
            return undefined;
          }
          if (id.includes('antd') || id.includes('@ant-design') || id.includes('rc-')) {
            return 'antd';
          }
          if (id.includes('react-router')) {
            return 'router';
          }
          if (id.includes('@tanstack')) {
            return 'tanstack';
          }
          if (id.includes('react-dom') || id.includes('/react/') || id.includes('scheduler')) {
            return 'react';
          }
          if (id.includes('dayjs') || id.includes('moment')) {
            return 'date';
          }
          return 'vendor';
        }
      }
    }
  }
});
