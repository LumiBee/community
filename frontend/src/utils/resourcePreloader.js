/**
 * 资源预加载工具
 * 解决Lighthouse报告中的渲染阻塞资源问题
 */

// 预加载CSS文件
export const preloadCSS = (href, media = 'all') => {
  const link = document.createElement('link');
  link.rel = 'preload';
  link.as = 'style';
  link.href = href;
  link.media = media;
  document.head.appendChild(link);
  
  // 延迟加载CSS以避免阻塞渲染
  setTimeout(() => {
    const styleLink = document.createElement('link');
    styleLink.rel = 'stylesheet';
    styleLink.href = href;
    styleLink.media = media;
    document.head.appendChild(styleLink);
  }, 100);
};

// 预加载JavaScript文件
export const preloadJS = (src, type = 'module') => {
  const link = document.createElement('link');
  link.rel = 'preload';
  link.as = 'script';
  link.href = src;
  if (type === 'module') {
    link.type = 'module';
  }
  document.head.appendChild(link);
  
  // 延迟加载JS以避免阻塞渲染
  setTimeout(() => {
    const script = document.createElement('script');
    script.src = src;
    if (type === 'module') {
      script.type = 'module';
    }
    document.head.appendChild(script);
  }, 200);
};

// 预加载字体
export const preloadFont = (href, type = 'font/woff2') => {
  const link = document.createElement('link');
  link.rel = 'preload';
  link.as = 'font';
  link.href = href;
  link.type = type;
  link.crossOrigin = 'anonymous';
  document.head.appendChild(link);
};

// 预加载关键资源
export const preloadCriticalResources = () => {
  // 预加载关键CSS
  const criticalCSS = [
    '/css/core.css',
    '/css/main.css'
  ];
  
  criticalCSS.forEach(href => preloadCSS(href));
  
  // 预加载关键字体
  const criticalFonts = [
    '/fonts/SourceSansPro-Regular.woff2',
    '/fonts/PlayfairDisplay-Regular.woff2'
  ];
  
  criticalFonts.forEach(href => preloadFont(href));
};

// 延迟加载非关键CSS
export const loadNonCriticalCSS = () => {
  const nonCriticalCSS = [
    '/css/article.css',
    '/css/portfolio.css',
    '/css/settings.css'
  ];
  
  // 使用requestIdleCallback在空闲时间加载
  if ('requestIdleCallback' in window) {
    requestIdleCallback(() => {
      nonCriticalCSS.forEach(href => preloadCSS(href));
    });
  } else {
    // 降级处理
    setTimeout(() => {
      nonCriticalCSS.forEach(href => preloadCSS(href));
    }, 3000);
  }
};

// 预加载下一页资源
export const preloadNextPage = (nextPageUrl) => {
  if (!nextPageUrl) return;
  
  // 预加载下一页的HTML
  const link = document.createElement('link');
  link.rel = 'prefetch';
  link.href = nextPageUrl;
  document.head.appendChild(link);
};

// 预加载图片资源
export const preloadImageResources = (imageUrls) => {
  imageUrls.forEach(url => {
    const link = document.createElement('link');
    link.rel = 'preload';
    link.as = 'image';
    link.href = url;
    document.head.appendChild(link);
  });
};

// 资源加载优先级管理
export const ResourcePriorityManager = {
  critical: [],
  high: [],
  normal: [],
  low: [],
  
  addResource(url, priority = 'normal', type = 'script') {
    const resource = { url, priority, type };
    
    switch (priority) {
      case 'critical':
        this.critical.push(resource);
        break;
      case 'high':
        this.high.push(resource);
        break;
      case 'normal':
        this.normal.push(resource);
        break;
      case 'low':
        this.low.push(resource);
        break;
    }
  },
  
  loadResources() {
    // 立即加载关键资源
    this.critical.forEach(resource => {
      if (resource.type === 'css') {
        preloadCSS(resource.url);
      } else if (resource.type === 'js') {
        preloadJS(resource.url);
      }
    });
    
    // 延迟加载高优先级资源
    setTimeout(() => {
      this.high.forEach(resource => {
        if (resource.type === 'css') {
          preloadCSS(resource.url);
        } else if (resource.type === 'js') {
          preloadJS(resource.url);
        }
      });
    }, 100);
    
    // 在空闲时间加载普通资源
    if ('requestIdleCallback' in window) {
      requestIdleCallback(() => {
        this.normal.forEach(resource => {
          if (resource.type === 'css') {
            preloadCSS(resource.url);
          } else if (resource.type === 'js') {
            preloadJS(resource.url);
          }
        });
      });
    }
    
    // 延迟加载低优先级资源
    setTimeout(() => {
      this.low.forEach(resource => {
        if (resource.type === 'css') {
          preloadCSS(resource.url);
        } else if (resource.type === 'js') {
          preloadJS(resource.url);
        }
      });
    }, 5000);
  }
};

// 初始化资源预加载
export const initResourcePreloading = () => {
  // 预加载关键资源
  preloadCriticalResources();
  
  // 延迟加载非关键CSS
  loadNonCriticalCSS();
  
  // 设置资源优先级
  ResourcePriorityManager.addResource('/js/common-utils.js', 'critical', 'js');
  // 暂时注释掉有问题的functions.js，避免jQuery错误
  // ResourcePriorityManager.addResource('/js/functions.js', 'high', 'js');
  ResourcePriorityManager.addResource('/js/article-effects.js', 'normal', 'js');
  ResourcePriorityManager.addResource('/js/bubble.js', 'low', 'js');
  
  // 开始加载资源
  ResourcePriorityManager.loadResources();
};

export default {
  preloadCSS,
  preloadJS,
  preloadFont,
  preloadCriticalResources,
  loadNonCriticalCSS,
  preloadNextPage,
  preloadImageResources,
  ResourcePriorityManager,
  initResourcePreloading
};
