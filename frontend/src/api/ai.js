import request from './config'

// AI API模块
export const aiAPI = {
  // 生成文章摘要
  generateSummary: async (textContent, maxLength = 100) => {
    try {
      console.log('开始调用AI摘要接口，内容长度:', textContent.length)
      
      // 使用配置好的axios实例，而不是原生fetch
      const response = await request({
        url: '/api/ai/generate-summary-deepseek',
        method: 'post',
        data: {
          textContent,
          maxLength
        }
      })
      
      console.log('AI摘要接口响应数据:', response)
      
      // 检查响应格式
      if (response && response.summary) {
        return { summary: response.summary }
      } else {
        throw new Error('响应格式异常，缺少summary字段')
      }
    } catch (error) {
      console.error('AI生成摘要失败:', error)
      throw error
    }
  }
}
