import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { splitVendorChunkPlugin } from 'vite'

export default defineConfig({
  plugins: [
    vue(),
    splitVendorChunkPlugin()
  ],
  appType: 'spa', // 指定为SPA应用，自动处理路由
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    open: true, // 自动打开浏览器
    // 处理SPA路由，所有未匹配的路由都返回index.html
    fs: {
      allow: ['..']
    },
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
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('uploads proxy error', err);
          });
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            console.log('Sending uploads Request to the Target:', req.method, req.url);
          });
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            console.log('Received uploads Response from the Target:', proxyRes.statusCode, req.url);
          });
        }
      },
      // 内容页面代理（用于SSR内容获取）
      '/api/article': {
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
    sourcemap: false, // 生产环境关闭sourcemap
    minify: 'terser', // 使用terser进行更彻底的压缩
    terserOptions: {
      compress: {
        drop_console: true, // 移除console.log
        drop_debugger: true, // 移除debugger
        pure_funcs: ['console.log', 'console.info', 'console.debug'] // 移除特定函数调用
      }
    },
    rollupOptions: {
      output: {
        // 更智能的代码分割
        manualChunks: {
          // Vue核心库
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // UI框架
          'bootstrap-vendor': ['bootstrap', 'bootstrap-vue-next'],
          // 工具库
          'utils-vendor': ['axios', 'dompurify'],
          // 图标库
          'icons-vendor': ['@fortawesome/fontawesome-free'],
          // 其他第三方库
          'other-vendor': ['marked', 'highlight.js']
        },
        // 文件名优化
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]'
      }
    },
    // 启用CSS代码分割
    cssCodeSplit: true,
    // 设置块大小警告限制
    chunkSizeWarningLimit: 1000,
    // 启用压缩
    reportCompressedSize: true
  },
  // 预构建优化
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', 'axios', 'bootstrap', 'bootstrap-vue-next'],
    exclude: ['@fortawesome/fontawesome-free'] // 排除大体积的图标库
  }
})
