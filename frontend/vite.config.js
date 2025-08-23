import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    open: true, // 自动打开浏览器
    proxy: {
      // API接口代理
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('proxy error', err);
          });
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            console.log('Sending Request to the Target:', req.method, req.url);
          });
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            console.log('Received Response from the Target:', proxyRes.statusCode, req.url);
          });
        }
      },
      // 认证相关代理
      '/login': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
      },
      '/signup': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
      },
      '/logout': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
      },
      // OAuth2代理
      '/oauth2': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
      },
      '/login-process': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
      },
      // 文件上传代理
      '/uploads': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
      },
      // 内容页面代理（用于SSR内容获取）
      '/article': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
      },
      '/search': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          bootstrap: ['bootstrap', 'bootstrap-vue-next'],
          utils: ['axios', 'marked', 'dompurify']
        }
      }
    }
  }
})
