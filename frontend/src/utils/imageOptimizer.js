/**
 * 图片优化工具
 * 解决Lighthouse报告中的图片相关问题
 */

// 检测WebP支持
let webpSupported = null;

export const checkWebPSupport = async () => {
  if (webpSupported !== null) return webpSupported;
  
  try {
    const canvas = document.createElement('canvas');
    canvas.width = 1;
    canvas.height = 1;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = 'rgba(0,0,0,0)';
    ctx.fillRect(0, 0, 1, 1);
    
    const webpDataURL = canvas.toDataURL('image/webp');
    webpSupported = webpDataURL.indexOf('data:image/webp') === 0;
  } catch (e) {
    webpSupported = false;
  }
  
  return webpSupported;
};

// 获取优化后的图片URL
export const getOptimizedImageUrl = (originalUrl, options = {}) => {
  if (!originalUrl) return '';
  
  const {
    width,
    height,
    quality = 80,
    format = 'auto'
  } = options;
  
  // 如果支持WebP且请求WebP格式
  if (format === 'webp' || (format === 'auto' && webpSupported)) {
    // 这里可以集成图片CDN服务，如Cloudinary、Imgix等
    // 示例：将.jpg/.png转换为.webp
    if (originalUrl.match(/\.(jpg|jpeg|png)$/i)) {
      return originalUrl.replace(/\.(jpg|jpeg|png)$/i, '.webp');
    }
  }
  
  // 添加尺寸参数
  if (width || height) {
    const url = new URL(originalUrl, window.location.origin);
    if (width) url.searchParams.set('w', width);
    if (height) url.searchParams.set('h', height);
    if (quality) url.searchParams.set('q', quality);
    return url.toString();
  }
  
  return originalUrl;
};

// 图片懒加载
export const lazyLoadImages = (selector = 'img[data-src]') => {
  const images = document.querySelectorAll(selector);
  
  if ('IntersectionObserver' in window) {
    const imageObserver = new IntersectionObserver((entries, observer) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target;
          const src = img.dataset.src;
          const srcset = img.dataset.srcset;
          
          if (src) {
            img.src = src;
            img.removeAttribute('data-src');
          }
          
          if (srcset) {
            img.srcset = srcset;
            img.removeAttribute('data-srcset');
          }
          
          img.classList.remove('lazy');
          imageObserver.unobserve(img);
        }
      });
    });
    
    images.forEach(img => imageObserver.observe(img));
  } else {
    // 降级处理
    images.forEach(img => {
      const src = img.dataset.src;
      if (src) {
        img.src = src;
        img.removeAttribute('data-src');
      }
    });
  }
};

// 预加载关键图片
export const preloadCriticalImages = (urls) => {
  urls.forEach(url => {
    const link = document.createElement('link');
    link.rel = 'preload';
    link.as = 'image';
    link.href = url;
    link.fetchPriority = 'high';
    document.head.appendChild(link);
  });
};

// 预加载LCP图片
export const preloadLCPImage = (url) => {
  if (!url) return;
  
  const link = document.createElement('link');
  link.rel = 'preload';
  link.as = 'image';
  link.href = url;
  link.fetchPriority = 'high';
  document.head.appendChild(link);
};

// 批量转换图片为WebP
export const convertImagesToWebP = async (imageUrls) => {
  const isWebPSupported = await checkWebPSupport();
  if (!isWebPSupported) return imageUrls;
  
  return imageUrls.map(url => {
    if (url.match(/\.(jpg|jpeg|png)$/i)) {
      return url.replace(/\.(jpg|jpeg|png)$/i, '.webp');
    }
    return url;
  });
};

// 图片尺寸优化
export const optimizeImageDimensions = (img) => {
  if (!img) return;
  
  // 设置明确的宽高以避免布局偏移
  if (!img.width || !img.height) {
    const rect = img.getBoundingClientRect();
    if (rect.width > 0 && rect.height > 0) {
      img.style.width = rect.width + 'px';
      img.style.height = rect.height + 'px';
    }
  }
};

// 响应式图片处理
export const createResponsiveImages = (src, sizes = []) => {
  if (!src || sizes.length === 0) return src;
  
  const srcset = sizes
    .map(size => `${getOptimizedImageUrl(src, { width: size })} ${size}w`)
    .join(', ');
  
  return srcset;
};

// 图片加载状态管理
export const ImageLoader = {
  loading: new Set(),
  loaded: new Set(),
  failed: new Set(),
  
  addLoading(url) {
    this.loading.add(url);
  },
  
  markLoaded(url) {
    this.loading.delete(url);
    this.loaded.add(url);
  },
  
  markFailed(url) {
    this.loading.delete(url);
    this.failed.add(url);
  },
  
  isLoaded(url) {
    return this.loaded.has(url);
  },
  
  isLoading(url) {
    return this.loading.has(url);
  },
  
  hasFailed(url) {
    return this.failed.has(url);
  }
};

// 初始化图片优化
export const initImageOptimization = () => {
  // 检查WebP支持
  checkWebPSupport();
  
  // 设置全局图片加载事件
  document.addEventListener('DOMContentLoaded', () => {
    // 懒加载所有图片
    lazyLoadImages();
    
    // 优化现有图片
    document.querySelectorAll('img').forEach(optimizeImageDimensions);
  });
  
  // 监听动态添加的图片
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      mutation.addedNodes.forEach((node) => {
        if (node.nodeType === Node.ELEMENT_NODE) {
          if (node.tagName === 'IMG') {
            optimizeImageDimensions(node);
          }
          const images = node.querySelectorAll('img');
          images.forEach(optimizeImageDimensions);
        }
      });
    });
  });
  
  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
};

export default {
  checkWebPSupport,
  getOptimizedImageUrl,
  lazyLoadImages,
  preloadCriticalImages,
  preloadLCPImage,
  convertImagesToWebP,
  optimizeImageDimensions,
  createResponsiveImages,
  ImageLoader,
  initImageOptimization
};
