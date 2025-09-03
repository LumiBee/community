/**
 * 性能监控工具
 * 监控LCP、FID、CLS等关键性能指标
 */

// 性能指标数据
const performanceData = {
  lcp: null,
  fid: null,
  cls: null,
  fcp: null,
  ttfb: null,
  fmp: null
};

// 性能观察器
let lcpObserver = null;
let fidObserver = null;
let clsObserver = null;

// 初始化性能监控
export const initPerformanceMonitoring = () => {
  // 监控LCP (Largest Contentful Paint)
  if ('PerformanceObserver' in window) {
    try {
      lcpObserver = new PerformanceObserver((entryList) => {
        const entries = entryList.getEntries();
        const lastEntry = entries[entries.length - 1];
        performanceData.lcp = lastEntry.startTime;
        
        // 如果LCP超过2.5秒，记录警告
        if (performanceData.lcp > 2500) {
          console.warn('LCP性能警告:', performanceData.lcp + 'ms');
        }
      });
      lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] });
    } catch (e) {
      console.warn('LCP监控初始化失败:', e);
    }

    // 监控FID (First Input Delay)
    try {
      fidObserver = new PerformanceObserver((entryList) => {
        const entries = entryList.getEntries();
        entries.forEach((entry) => {
          performanceData.fid = entry.processingStart - entry.startTime;
          
          // 如果FID超过100ms，记录警告
          if (performanceData.fid > 100) {
            console.warn('FID性能警告:', performanceData.fid + 'ms');
          }
        });
      });
      fidObserver.observe({ entryTypes: ['first-input'] });
    } catch (e) {
      console.warn('FID监控初始化失败:', e);
    }

    // 监控CLS (Cumulative Layout Shift)
    try {
      clsObserver = new PerformanceObserver((entryList) => {
        let clsValue = 0;
        const entries = entryList.getEntries();
        
        entries.forEach((entry) => {
          if (!entry.hadRecentInput) {
            clsValue += entry.value;
          }
        });
        
        performanceData.cls = clsValue;
        
        // 如果CLS超过0.1，记录警告
        if (performanceData.cls > 0.1) {
          console.warn('CLS性能警告:', performanceData.cls);
        }
      });
      clsObserver.observe({ entryTypes: ['layout-shift'] });
    } catch (e) {
      console.warn('CLS监控初始化失败:', e);
    }
  }

  // 监控FCP (First Contentful Paint)
  if ('PerformanceObserver' in window) {
    try {
      const fcpObserver = new PerformanceObserver((entryList) => {
        const entries = entryList.getEntries();
        performanceData.fcp = entries[0].startTime;
      });
      fcpObserver.observe({ entryTypes: ['first-contentful-paint'] });
    } catch (e) {
      console.warn('FCP监控初始化失败:', e);
    }
  }

  // 监控TTFB (Time to First Byte)
  if ('PerformanceObserver' in window) {
    try {
      const ttfbObserver = new PerformanceObserver((entryList) => {
        const entries = entryList.getEntries();
        entries.forEach((entry) => {
          if (entry.entryType === 'navigation') {
            performanceData.ttfb = entry.responseStart - entry.requestStart;
          }
        });
      });
      ttfbObserver.observe({ entryTypes: ['navigation'] });
    } catch (e) {
      console.warn('TTFB监控初始化失败:', e);
    }
  }
};

// 获取性能数据
export const getPerformanceData = () => {
  return { ...performanceData };
};

// 获取性能评分
export const getPerformanceScore = () => {
  let score = 100;
  
  // LCP评分 (权重: 25%)
  if (performanceData.lcp) {
    if (performanceData.lcp <= 2500) score -= 0;
    else if (performanceData.lcp <= 4000) score -= 10;
    else score -= 25;
  }
  
  // FID评分 (权重: 25%)
  if (performanceData.fid) {
    if (performanceData.fid <= 100) score -= 0;
    else if (performanceData.fid <= 300) score -= 10;
    else score -= 25;
  }
  
  // CLS评分 (权重: 25%)
  if (performanceData.cls) {
    if (performanceData.cls <= 0.1) score -= 0;
    else if (performanceData.cls <= 0.25) score -= 10;
    else score -= 25;
  }
  
  // FCP评分 (权重: 15%)
  if (performanceData.fcp) {
    if (performanceData.fcp <= 1800) score -= 0;
    else if (performanceData.fcp <= 3000) score -= 5;
    else score -= 15;
  }
  
  // TTFB评分 (权重: 10%)
  if (performanceData.ttfb) {
    if (performanceData.ttfb <= 800) score -= 0;
    else if (performanceData.ttfb <= 1800) score -= 5;
    else score -= 10;
  }
  
  return Math.max(0, score);
};

// 生成性能报告
export const generatePerformanceReport = () => {
  const score = getPerformanceScore();
  const data = getPerformanceData();
  
  const report = {
    timestamp: new Date().toISOString(),
    score: score,
    grade: score >= 90 ? 'A' : score >= 80 ? 'B' : score >= 70 ? 'C' : score >= 60 ? 'D' : 'F',
    metrics: {
      lcp: {
        value: data.lcp,
        unit: 'ms',
        status: data.lcp <= 2500 ? 'good' : data.lcp <= 4000 ? 'needs-improvement' : 'poor'
      },
      fid: {
        value: data.fid,
        unit: 'ms',
        status: data.fid <= 100 ? 'good' : data.fid <= 300 ? 'needs-improvement' : 'poor'
      },
      cls: {
        value: data.cls,
        unit: '',
        status: data.cls <= 0.1 ? 'good' : data.cls <= 0.25 ? 'needs-improvement' : 'poor'
      },
      fcp: {
        value: data.fcp,
        unit: 'ms',
        status: data.fcp <= 1800 ? 'good' : data.fcp <= 3000 ? 'needs-improvement' : 'poor'
      },
      ttfb: {
        value: data.ttfb,
        unit: 'ms',
        status: data.ttfb <= 800 ? 'good' : data.ttfb <= 1800 ? 'needs-improvement' : 'poor'
      }
    },
    recommendations: []
  };
  
  // 生成优化建议
  if (data.lcp > 2500) {
    report.recommendations.push({
      priority: 'high',
      category: 'LCP',
      title: '优化最大内容绘制',
      description: 'LCP时间过长，建议优化图片加载、减少渲染阻塞资源',
      impact: 'high'
    });
  }
  
  if (data.fid > 100) {
    report.recommendations.push({
      priority: 'high',
      category: 'FID',
      title: '优化首次输入延迟',
      description: '用户交互响应慢，建议减少JavaScript执行时间',
      impact: 'high'
    });
  }
  
  if (data.cls > 0.1) {
    report.recommendations.push({
      priority: 'medium',
      category: 'CLS',
      title: '减少布局偏移',
      description: '页面元素位置不稳定，建议设置明确的图片尺寸',
      impact: 'medium'
    });
  }
  
  return report;
};

// 发送性能数据到分析服务
export const sendPerformanceData = (endpoint = '/performance') => {
  // 暂时禁用性能数据发送，因为后端没有对应的API
  console.log('性能数据收集完成，但未发送到服务器（后端API未实现）');
  return;
  
  const report = generatePerformanceReport();
  
  if ('navigator' in window && 'sendBeacon' in navigator) {
    navigator.sendBeacon(endpoint, JSON.stringify(report));
  } else {
    // 降级到fetch
    fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(report)
    }).catch(console.error);
  }
};

// 清理监控器
export const cleanupPerformanceMonitoring = () => {
  if (lcpObserver) {
    lcpObserver.disconnect();
    lcpObserver = null;
  }
  if (fidObserver) {
    fidObserver.disconnect();
    fidObserver = null;
  }
  if (clsObserver) {
    clsObserver.disconnect();
    clsObserver = null;
  }
};

// 页面可见性变化时的性能监控
export const initVisibilityPerformanceMonitoring = () => {
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'hidden') {
      // 页面隐藏时发送性能数据
      sendPerformanceData();
    }
  });
};

// 页面卸载时的性能监控
export const initUnloadPerformanceMonitoring = () => {
  window.addEventListener('beforeunload', () => {
    sendPerformanceData();
  });
};

export default {
  initPerformanceMonitoring,
  getPerformanceData,
  getPerformanceScore,
  generatePerformanceReport,
  sendPerformanceData,
  cleanupPerformanceMonitoring,
  initVisibilityPerformanceMonitoring,
  initUnloadPerformanceMonitoring
};
