/**
 * 性能优化工具函数
 */

// 图片懒加载
export const lazyLoadImages = () => {
  if ('IntersectionObserver' in window) {
    const imageObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target
          if (img.dataset.src) {
            img.src = img.dataset.src
            img.classList.remove('lazy')
            imageObserver.unobserve(img)
          }
        }
      })
    })

    // 观察所有懒加载图片
    document.querySelectorAll('img[data-src]').forEach(img => {
      imageObserver.observe(img)
    })
  }
}

// 预加载关键图片
export const preloadCriticalImages = (urls) => {
  urls.forEach(url => {
    const link = document.createElement('link')
    link.rel = 'preload'
    link.as = 'image'
    link.href = url
    document.head.appendChild(link)
  })
}

// 预加载LCP图片
export const preloadLCPImage = (url) => {
  if (url) {
    const link = document.createElement('link')
    link.rel = 'preload'
    link.as = 'image'
    link.href = url
    link.fetchPriority = 'high'
    document.head.appendChild(link)
  }
}

// 检查WebP支持
export const supportsWebP = () => {
  return new Promise((resolve) => {
    const webP = new Image()
    webP.onload = webP.onerror = () => {
      resolve(webP.height === 2)
    }
    webP.src = 'data:image/webp;base64,UklGRjoAAABXRUJQVlA4IC4AAACyAgCdASoCAAIALmk0mk0iIiIiIgBoSygABc6WWgAA/veff/0PP8bA//LwIDAAA='
  })
}

// 获取优化的图片URL
export const getOptimizedImageUrl = async (url, format = 'webp') => {
  if (!url) return url
  
  // 如果是外部图片，直接返回
  if (url.startsWith('http')) return url
  
  // 检查WebP支持
  if (format === 'webp' && url.match(/\.(jpg|jpeg|png)$/i)) {
    const webpSupported = await supportsWebP()
    if (webpSupported) {
      return url.replace(/\.(jpg|jpeg|png)$/i, '.webp')
    }
  }
  
  return url
}

// 批量转换图片为WebP
export const convertImagesToWebP = async (imageUrls) => {
  const webpSupported = await supportsWebP()
  if (!webpSupported) return imageUrls
  
  return imageUrls.map(url => {
    if (url.match(/\.(jpg|jpeg|png)$/i)) {
      return url.replace(/\.(jpg|jpeg|png)$/i, '.webp')
    }
    return url
  })
}

// 防抖函数
export const debounce = (func, wait) => {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

// 节流函数
export const throttle = (func, limit) => {
  let inThrottle
  return function() {
    const args = arguments
    const context = this
    if (!inThrottle) {
      func.apply(context, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// 性能监控
export const performanceMonitor = {
  // 记录LCP时间
  recordLCP: () => {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries()
        const lastEntry = entries[entries.length - 1]
        console.log('LCP:', lastEntry.startTime)
        
        // 发送到分析服务
        if (lastEntry.startTime > 2500) {
          console.warn('LCP too slow:', lastEntry.startTime)
        }
      })
      
      observer.observe({ entryTypes: ['largest-contentful-paint'] })
    }
  },
  
  // 记录FID时间
  recordFID: () => {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries()
        entries.forEach(entry => {
          console.log('FID:', entry.processingStart - entry.startTime)
          
          if (entry.processingStart - entry.startTime > 100) {
            console.warn('FID too slow:', entry.processingStart - entry.startTime)
          }
        })
      })
      
      observer.observe({ entryTypes: ['first-input'] })
    }
  },
  
  // 记录CLS
  recordCLS: () => {
    if ('PerformanceObserver' in window) {
      let clsValue = 0
      const observer = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (!entry.hadRecentInput) {
            clsValue += entry.value
          }
        }
        
        if (clsValue > 0.1) {
          console.warn('CLS too high:', clsValue)
        }
      })
      
      observer.observe({ entryTypes: ['layout-shift'] })
    }
  }
}

// 资源预加载
export const preloadResources = {
  // 预加载CSS
  css: (url) => {
    const link = document.createElement('link')
    link.rel = 'preload'
    link.as = 'style'
    link.href = url
    document.head.appendChild(link)
  },
  
  // 预加载JavaScript
  js: (url) => {
    const link = document.createElement('link')
    link.rel = 'preload'
    link.as = 'script'
    link.href = url
    document.head.appendChild(link)
  },
  
  // 预加载字体
  font: (url) => {
    const link = document.createElement('link')
    link.rel = 'preload'
    link.as = 'font'
    link.href = url
    link.crossOrigin = 'anonymous'
    document.head.appendChild(link)
  }
}

// 初始化性能监控
export const initPerformanceMonitoring = () => {
  performanceMonitor.recordLCP()
  performanceMonitor.recordFID()
  performanceMonitor.recordCLS()
  
  // 监听页面可见性变化
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      // 页面变为可见时，可以执行一些优化操作
      console.log('Page became visible')
    }
  })
}
