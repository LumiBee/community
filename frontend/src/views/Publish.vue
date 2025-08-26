<template>
  <div class="publish-page">
    <!-- 页面头部 -->
    <div class="publish-header">
      <div class="header-content">
        <h1 class="page-title">
          <i class="fas fa-pen-fancy me-2"></i>
          发布新文章
        </h1>
      </div>
      <div class="header-actions">
        <button 
          @click="saveDraft" 
          class="btn btn-outline-secondary me-2"
          :disabled="isSaving"
        >
          <i class="fas fa-save me-1"></i>
          {{ isSaving ? '保存中...' : '保存草稿' }}
        </button>
        <button 
          @click="showPublishModal = true" 
          class="btn btn-warning"
          :disabled="!canPublish"
        >
          <i class="fas fa-paper-plane me-1"></i>
          发布文章
        </button>
      </div>
    </div>

    <!-- 通知区域 -->
    <div 
      v-if="notification.show" 
      class="alert alert-dismissible fade show"
      :class="`alert-${notification.type}`"
      role="alert"
    >
      <i 
        :class="notification.type === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-circle'"
        class="me-2"
      ></i>
      {{ notification.message }}
      <button 
        type="button" 
        class="btn-close" 
        @click="notification.show = false"
      ></button>
    </div>

    <!-- 主要内容区域 - 左右分栏布局 -->
    <div class="publish-content">
      <div class="row g-0">
        <!-- 左侧编辑区域 -->
        <div class="col-md-6">
          <div class="edit-section">
            <!-- 文章标题 -->
            <div class="form-group mb-3">
              <label for="articleTitle" class="form-label">
                <i class="fas fa-heading me-2"></i>文章标题
              </label>
              <input
                type="text"
                id="articleTitle"
                v-model="articleForm.title"
                class="form-control form-control-lg"
                placeholder="请输入文章标题..."
                maxlength="100"
              />
            </div>

            <!-- 文章内容 -->
            <div class="form-group mb-3">
              <label for="articleContent" class="form-label">
                <i class="fas fa-edit me-2"></i>文章内容
              </label>
              
              <!-- 工具栏 -->
              <div class="editor-toolbar-wrapper mb-2">
                <div class="editor-toolbar">
                  <button 
                    type="button" 
                    class="btn btn-sm btn-outline-secondary me-1"
                    @click="insertMarkdown('**', '**', '粗体')"
                    title="粗体"
                  >
                    <i class="fas fa-bold"></i>
                  </button>
                  <button 
                    type="button" 
                    class="btn btn-sm btn-outline-secondary me-1"
                    @click="insertMarkdown('*', '*', '斜体')"
                    title="斜体"
                  >
                    <i class="fas fa-italic"></i>
                  </button>
                  <button 
                    type="button" 
                    class="btn btn-sm btn-outline-secondary me-1"
                    @click="insertMarkdown('### ', '', '标题')"
                    title="标题"
                  >
                    <i class="fas fa-heading"></i>
                  </button>
                  <button 
                    type="button" 
                    class="btn btn-sm btn-outline-secondary me-1"
                    @click="insertMarkdown('[', '](url)', '链接')"
                    title="链接"
                  >
                    <i class="fas fa-link"></i>
                  </button>
                  <button 
                    type="button" 
                    class="btn btn-sm btn-outline-secondary me-1"
                    @click="insertMarkdown('![alt](', ')', '图片')"
                    title="图片"
                  >
                    <i class="fas fa-image"></i>
                  </button>
                  <button 
                    type="button" 
                    class="btn btn-sm btn-outline-secondary me-1"
                    @click="insertMarkdown('```\n', '\n```', '代码块')"
                    title="代码块"
                  >
                    <i class="fas fa-code"></i>
                  </button>
                  <button 
                    type="button" 
                    class="btn btn-sm btn-outline-secondary me-1"
                    @click="insertMarkdown('- ', '', '列表项')"
                    title="列表项"
                  >
                    <i class="fas fa-list"></i>
                  </button>
                  <button 
                    type="button" 
                    class="btn btn-sm btn-outline-secondary me-1"
                    @click="insertMarkdown('> ', '', '引用')"
                    title="引用"
                  >
                    <i class="fas fa-quote-right"></i>
                  </button>
                </div>
              </div>
              
              <!-- 编辑器 -->
              <div class="content-editor-wrapper">
                <textarea
                  id="articleContent"
                  v-model="articleForm.content"
                  class="form-control content-editor"
                  placeholder="请输入文章内容，支持Markdown格式..."
                  @input="updateWordCount"
                ></textarea>
              </div>
              <div class="form-text mt-2">
                <span class="text-muted">
                  <i class="fas fa-info-circle me-1"></i>
                  支持Markdown格式，已输入 {{ wordCount }} 字
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧实时预览区域 -->
        <div class="col-md-6">
          <div class="preview-section">
            <div class="preview-header">
              <h5 class="preview-title">
                <i class="fas fa-eye me-2"></i>实时预览
              </h5>
              <div class="preview-actions">
                <button 
                  type="button" 
                  class="btn btn-sm btn-outline-secondary"
                  @click="togglePreviewMode"
                  :title="previewMode === 'article' ? '切换到完整预览' : '切换到文章预览'"
                >
                  <i :class="previewMode === 'article' ? 'fas fa-expand' : 'fas fa-compress'"></i>
                  {{ previewMode === 'article' ? '完整预览' : '文章预览' }}
                </button>
              </div>
            </div>
            <div class="preview-content">
              <div v-if="previewMode === 'article'" class="article-preview">
                <h1 class="preview-title-text">{{ articleForm.title || '文章标题' }}</h1>
                <div v-if="articleForm.excerpt" class="preview-excerpt">
                  {{ articleForm.excerpt }}
                </div>
                <div class="preview-body" v-html="renderedContent"></div>
              </div>
              <div v-else class="content-preview">
                <div class="preview-body" v-html="renderedContent"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 隐藏的图片上传输入框 -->
    <input
      ref="imageUploadInput"
      type="file"
      accept="image/*"
      class="d-none"
      @change="handleImageUpload"
    />

    <!-- 发布设置模态框 -->
    <div 
      v-if="showPublishModal" 
      class="modal fade show" 
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="fas fa-cog me-2"></i>发布设置
            </h5>
            <button 
              type="button" 
              class="btn-close" 
              @click="showPublishModal = false"
            ></button>
          </div>
          <div class="modal-body">
            <div class="row">
              <!-- 左侧设置 -->
              <div class="col-md-6">
                <!-- 文章摘要 -->
                <div class="setting-card mb-4">
                  <h6 class="setting-title">
                    <i class="fas fa-align-left me-2"></i>文章摘要
                    <span class="text-muted ms-2" style="font-size: 0.9rem; font-weight: normal;">(可选)</span>
                  </h6>
                  <div class="form-group mb-0">
                    <div class="ai-summary-buttons">
                      <button 
                        type="button" 
                        class="btn btn-sm btn-outline-primary"
                        @click="generateAISummary"
                        :disabled="!articleForm.content || isGeneratingSummary"
                        title="使用AI自动生成摘要"
                      >
                        <i class="fas fa-magic me-1"></i>
                        {{ isGeneratingSummary ? '生成中...' : 'AI生成摘要' }}
                      </button>
                      <button 
                        v-if="articleForm.excerpt"
                        type="button" 
                        class="btn btn-sm btn-outline-secondary"
                        @click="clearExcerpt"
                        title="清空摘要"
                      >
                        <i class="fas fa-trash me-1"></i>
                        清空
                      </button>
                    </div>
                    <textarea
                      v-model="articleForm.excerpt"
                      class="form-control"
                      rows="3"
                      placeholder="请输入文章摘要...（可选）或点击上方按钮使用AI生成"
                      maxlength="300"
                    ></textarea>
                    <div class="form-text">
                      <span :class="{ 'text-danger': articleForm.excerpt.length > 300 }">
                        {{ articleForm.excerpt.length }}/300
                      </span>
                    </div>
                  </div>
                </div>

                <!-- 封面图片 -->
                <div class="setting-card mb-4">
                  <h6 class="setting-title">
                    <i class="fas fa-image me-2"></i>封面图片
                  </h6>
                  <div class="cover-upload">
                    <div 
                      class="cover-preview"
                      :style="{ backgroundImage: `url(${coverPreview})` }"
                      @click="triggerCoverUpload"
                    >
                      <div v-if="!coverPreview" class="cover-placeholder">
                        <i class="fas fa-camera"></i>
                        <p>点击上传封面</p>
                      </div>
                    </div>
                    <input
                      ref="coverInput"
                      type="file"
                      accept="image/*"
                      class="d-none"
                      @change="handleCoverUpload"
                    />
                    <div class="cover-actions mt-2">
                      <button 
                        v-if="coverPreview" 
                        @click="removeCover" 
                        class="btn btn-sm btn-outline-danger"
                      >
                        <i class="fas fa-trash me-1"></i>移除
                      </button>
                    </div>
                  </div>
                </div>

                <!-- 标签 -->
                <div class="setting-card mb-4">
                  <h6 class="setting-title">
                    <i class="fas fa-tags me-2"></i>标签
                  </h6>
                  <div class="tags-input">
                    <div class="tags-container">
                      <span 
                        v-for="tag in articleForm.tags" 
                        :key="tag"
                        class="tag-item"
                      >
                        {{ tag }}
                        <button 
                          @click="removeTag(tag)" 
                          class="tag-remove"
                          type="button"
                        >
                          <i class="fas fa-times"></i>
                        </button>
                      </span>
                    </div>
                    <input
                      v-model="newTag"
                      @keyup.enter="addTag"
                      @blur="addTag"
                      class="form-control"
                      placeholder="输入标签后按回车..."
                    />
                  </div>
                </div>
              </div>

              <!-- 右侧设置 -->
              <div class="col-md-6">
                <!-- 作品集 -->
                <div class="setting-card mb-4">
                  <h6 class="setting-title">
                    <i class="fas fa-briefcase me-2"></i>作品集
                  </h6>
                  <select v-model="articleForm.portfolioId" class="form-select">
                    <option value="">选择作品集（可选）</option>
                    <option 
                      v-for="portfolio in portfolios" 
                      :key="portfolio.id" 
                      :value="portfolio.id"
                    >
                      {{ portfolio.title }}
                    </option>
                  </select>
                </div>

                <!-- 发布设置 -->
                <div class="setting-card mb-4">
                  <h6 class="setting-title">
                    <i class="fas fa-cog me-2"></i>发布设置
                  </h6>
                  <div class="form-check">
                    <input
                      type="checkbox"
                      id="allowComments"
                      v-model="articleForm.allowComments"
                      class="form-check-input"
                    />
                    <label class="form-check-label" for="allowComments">
                      允许评论
                    </label>
                  </div>
                </div>

                <!-- 发布确认 -->
                <div class="setting-card">
                  <h6 class="setting-title">
                    <i class="fas fa-check-circle me-2"></i>发布确认
                  </h6>
                  <div class="publish-confirmation">
                    <p class="text-muted mb-3">
                      请确认以下信息无误后点击发布：
                    </p>
                    <ul class="confirmation-list">
                      <li :class="getStatusClass('title')">
                        <i class="fas fa-check me-2"></i>
                        标题：{{ articleForm.title || '未填写' }}
                      </li>
                      <li :class="getStatusClass('content')">
                        <i class="fas fa-check me-2"></i>
                        内容：{{ articleForm.content ? `${wordCount} 字` : '未填写' }}
                      </li>
                      <li :class="getStatusClass('excerpt')">
                        <i class="fas fa-check me-2"></i>
                        摘要：{{ getStatusText('excerpt') }}
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button 
              type="button" 
              class="btn btn-secondary" 
              @click="showPublishModal = false"
            >
              取消
            </button>
            <button 
              type="button" 
              class="btn btn-warning" 
              @click="confirmPublish"
              :disabled="!canPublish || isPublishing"
            >
              <i class="fas fa-paper-plane me-1"></i>
              {{ isPublishing ? '发布中...' : '确认发布' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showPublishModal" class="modal-backdrop fade show"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { articleAPI, portfolioAPI, aiAPI } from '@/api'
import MarkdownIt from 'markdown-it'

const router = useRouter()
const authStore = useAuthStore()
const md = new MarkdownIt()

// 响应式数据
const articleForm = ref({
  title: '',
  excerpt: '',
  content: '',
  tags: [],
  portfolioId: '',
  allowComments: true
})

const coverInput = ref(null)
const coverPreview = ref('')
const newTag = ref('')
const portfolios = ref([])
const isSaving = ref(false)
const isPublishing = ref(false)
const isGeneratingSummary = ref(false)
const showPublishModal = ref(false)
const wordCount = ref(0)
const notification = ref({ show: false, message: '', type: 'success' })

// 预览模式
const previewMode = ref('article') // 'article' 或 'content'

// 计算属性
const canPublish = computed(() => {
  return articleForm.value.title.trim() && 
         articleForm.value.content.trim()
})

const renderedContent = computed(() => {
  return md.render(articleForm.value.content || '')
})

// 方法
const updateWordCount = () => {
  const content = articleForm.value.content || ''
  wordCount.value = content.replace(/\s/g, '').length
}

const togglePreviewMode = () => {
  previewMode.value = previewMode.value === 'article' ? 'content' : 'article'
}

const insertMarkdown = (before, after, description) => {
  const textarea = document.getElementById('articleContent')
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = articleForm.value.content.substring(start, end)
  
  const newText = before + selectedText + after
  articleForm.value.content = 
    articleForm.value.content.substring(0, start) + 
    newText + 
    articleForm.value.content.substring(end)
  
  // 设置光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selectedText.length)
  }, 0)
}

// 图片上传逻辑
const imageUploadInput = ref(null)
const isImageUploading = ref(false)

const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (file && file.type.startsWith('image/')) {
    isImageUploading.value = true
    const reader = new FileReader()
    reader.onload = (e) => {
      insertImageMarkdown(e.target.result, file.name)
      isImageUploading.value = false
    }
    reader.onerror = () => {
      isImageUploading.value = false
      showNotification('图片上传失败，请重试', 'danger')
    }
    reader.readAsDataURL(file)
  }
}

// 插入图片Markdown
const insertImageMarkdown = (imageData, fileName = '图片') => {
  const textarea = document.getElementById('articleContent')
  const start = textarea.selectionStart
  const imageMarkdown = `![${fileName}](${imageData})`
  
  // 在光标位置插入图片Markdown
  articleForm.value.content = 
    articleForm.value.content.substring(0, start) + 
    imageMarkdown + 
    articleForm.value.content.substring(start)
  
  // 设置光标位置到图片后面
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + imageMarkdown.length, start + imageMarkdown.length)
  }, 0)
  
  showNotification('图片已插入到文章中', 'success')
}

const triggerCoverUpload = () => {
  coverInput.value.click()
}

const handleCoverUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      coverPreview.value = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

const removeCover = () => {
  coverPreview.value = ''
  if (coverInput.value) {
    coverInput.value.value = ''
  }
}

const addTag = () => {
  const tag = newTag.value.trim()
  if (tag && !articleForm.value.tags.includes(tag)) {
    articleForm.value.tags.push(tag)
    newTag.value = ''
  }
}

const removeTag = (tag) => {
  const index = articleForm.value.tags.indexOf(tag)
  if (index > -1) {
    articleForm.value.tags.splice(index, 1)
  }
}

const getStatusClass = (field) => {
  const value = articleForm.value[field]
  if (field === 'excerpt') {
    // 摘要字段是可选的，空值不显示错误
    if (value && value.trim() !== '' && value.length > 300) return 'text-warning'
    return 'text-success'
  }
  if (!value || value.trim() === '') return 'text-danger'
  if (field === 'title' && value.length > 100) return 'text-warning'
  return 'text-success'
}

const getStatusText = (field) => {
  const value = articleForm.value[field]
  if (field === 'excerpt') {
    // 摘要字段是可选的
    if (!value || value.trim() === '') return '未填写（可选）'
    if (value.length > 300) return '摘要过长'
    return '已填写'
  }
  if (!value || value.trim() === '') return '未填写'
  if (field === 'title' && value.length > 100) return '标题过长'
  return '已填写'
}

const showNotification = (message, type = 'success') => {
  notification.value = { show: true, message, type }
  setTimeout(() => {
    notification.value.show = false
  }, 3000)
}

const saveDraft = async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  try {
    isSaving.value = true
    const response = await articleAPI.saveDraft(articleForm.value)
    
    if (response.success) {
      showNotification('草稿保存成功！', 'success')
    } else {
      showNotification('保存失败，请重试', 'danger')
    }
  } catch (error) {
    console.error('保存草稿失败:', error)
    showNotification('保存失败：' + (error.message || '未知错误'), 'danger')
  } finally {
    isSaving.value = false
  }
}

const confirmPublish = async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  try {
    isPublishing.value = true
    
    // 如果用户没有填写摘要，自动调用AI生成摘要
    let finalExcerpt = articleForm.value.excerpt
    if (!finalExcerpt || finalExcerpt.trim() === '') {
      try {
        showNotification('正在使用AI生成文章摘要...', 'info')
        
        // 使用aiAPI模块中的generateSummary方法
        const plainTextContent = articleForm.value.content.replace(/<\/?[^>]+(>|$)/g, "").trim()
        
        try {
          // 使用导入的aiAPI模块
          const result = await aiAPI.generateSummary(plainTextContent, 300)
          
          if (result && result.summary) {
            finalExcerpt = result.summary
            articleForm.value.excerpt = finalExcerpt
            showNotification('AI摘要生成成功！', 'success')
          } else {
            throw new Error('未能获取有效的摘要')
          }
        } catch (error) {
          // 如果AI生成失败，使用内容的前100个字符作为摘要
          console.warn('发布时AI摘要生成失败，使用内容前100字符作为摘要:', error)
          finalExcerpt = plainTextContent.substring(0, 100).replace(/[#*`]/g, '') + '...'
          articleForm.value.excerpt = finalExcerpt
          showNotification('AI摘要生成失败，已使用文章开头作为摘要', 'warning')
        }
      } catch (aiError) {
        console.warn('AI生成摘要失败，使用默认摘要:', aiError)
        // AI生成失败时，使用内容的前100个字符作为摘要
        finalExcerpt = articleForm.value.content.substring(0, 100).replace(/[#*`]/g, '') + '...'
        articleForm.value.excerpt = finalExcerpt
      }
    }
    
    // 准备发布数据
    const publishData = {
      ...articleForm.value,
      excerpt: finalExcerpt,
      coverImg: coverPreview.value
    }
    
    const response = await articleAPI.publishArticle(publishData)
    
    // 后端直接返回文章对象，不是包含 success 字段的响应
    if (response && response.articleId) {
      showNotification('文章发布成功！', 'success')
      showPublishModal.value = false
      // 发布成功，跳转到文章页面
      setTimeout(() => {
        router.push(`/article/${response.slug}`)
      }, 200)
    } else {
      showNotification('发布失败，请重试', 'danger')
    }
  } catch (error) {
    console.error('发布文章失败:', error)
    showNotification('发布失败：' + (error.message || '未知错误'), 'danger')
  } finally {
    isPublishing.value = false
  }
}

const loadPortfolios = async () => {
  try {
    const response = await portfolioAPI.getAllPortfolios()
    portfolios.value = response || []
  } catch (error) {
    console.error('加载作品集失败:', error)
  }
}

// AI生成摘要方法 - 参照publish-editor.js的实现
const generateAISummary = async () => {
  if (!articleForm.value.content || articleForm.value.content.trim() === '') {
    showNotification('请先输入文章内容', 'warning')
    return
  }

  try {
    isGeneratingSummary.value = true
    showNotification('正在使用AI生成摘要...', 'info')
    
    console.log('开始调用AI生成摘要，内容长度:', articleForm.value.content.length)
    
    // 使用aiAPI模块中的generateSummary方法，而不是直接使用fetch
    const plainTextContent = articleForm.value.content.replace(/<\/?[^>]+(>|$)/g, "").trim()
    
    try {
      // 使用导入的aiAPI模块
      const result = await aiAPI.generateSummary(plainTextContent, 300)
      
      if (result && result.summary) {
        articleForm.value.excerpt = result.summary
        showNotification('AI摘要生成成功！', 'success')
      } else {
        throw new Error('未能获取有效的摘要')
      }
    } catch (error) {
      // 如果AI生成失败，使用内容的前100个字符作为摘要
      console.error('AI摘要生成失败，使用内容前100字符作为摘要:', error)
      articleForm.value.excerpt = plainTextContent.substring(0, 100).replace(/[#*`]/g, '') + '...'
      showNotification('AI摘要生成失败，已使用文章开头作为摘要', 'warning')
    }
  } catch (error) {
    console.error('AI生成摘要失败:', error)
    showNotification('AI生成摘要失败：' + (error.message || '未知错误'), 'danger')
  } finally {
    isGeneratingSummary.value = false
  }
}

// 清空摘要方法
const clearExcerpt = () => {
  articleForm.value.excerpt = ''
  showNotification('摘要已清空', 'info')
}

// 组件挂载
onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  
  loadPortfolios()
})
</script>

<style scoped>
.publish-page {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  height: 100vh;
  padding: 1rem 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.publish-header {
  background: white;
  border-radius: 16px;
  padding: 1rem 2rem;
  margin-bottom: 1rem;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  margin-left: 1rem;
  margin-right: 1rem;
  flex-shrink: 0;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(45deg, #343a40, #495057);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #6c757d;
  margin: 0.5rem 0 0 0;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

/* 通知样式 */
.alert {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin: 0 1rem 1rem 1rem;
  flex-shrink: 0;
}

.alert-success {
  background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
  color: #155724;
}

.alert-danger {
  background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%);
  color: #721c24;
}

.alert-warning {
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
  color: #856404;
}

.publish-content {
  background: white;
  border-radius: 16px;
  margin: 0 1rem;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  overflow: hidden;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.edit-section {
  padding: 1rem;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.preview-section {
  height: 100%;
  border-left: 1px solid #e9ecef;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.preview-header {
  background: white;
  padding: 0.75rem 1.5rem;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.preview-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #495057;
  margin: 0;
}

.preview-actions {
  display: flex;
  gap: 0.5rem;
}

.preview-content {
  height: 100%;
  overflow-y: auto;
  padding: 1rem;
  flex: 1;
  min-height: 700px;
  max-height: calc(100vh - 200px);
}

/* 自定义滚动条样式 */
.content-editor::-webkit-scrollbar,
.preview-content::-webkit-scrollbar {
  width: 8px;
}

.content-editor::-webkit-scrollbar-track,
.preview-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.content-editor::-webkit-scrollbar-thumb,
.preview-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.content-editor::-webkit-scrollbar-thumb:hover,
.preview-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 确保预览内容区域在Firefox和其他浏览器中也能正确滚动 */
.preview-content {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}

/* 确保预览内容区域在内容过多时能够正确滚动 */
.preview-content .article-preview,
.preview-content .content-preview {
  max-height: none;
  overflow: visible;
}

.article-preview {
  background: white;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.preview-title-text {
  font-size: 2rem;
  font-weight: 700;
  color: #343a40;
  margin-bottom: 1rem;
  border-bottom: 2px solid #f6d55c;
  padding-bottom: 0.5rem;
}

.preview-excerpt {
  font-size: 1.1rem;
  color: #6c757d;
  font-style: italic;
  margin-bottom: 2rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #f6d55c;
}

.preview-body {
  line-height: 1.8;
  color: #495057;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.preview-body h1,
.preview-body h2,
.preview-body h3,
.preview-body h4,
.preview-body h5,
.preview-body h6 {
  color: #343a40;
  margin-top: 2rem;
  margin-bottom: 1rem;
}

.preview-body h1 {
  font-size: 1.8rem;
  border-bottom: 2px solid #e9ecef;
  padding-bottom: 0.5rem;
}

.preview-body h2 {
  font-size: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  padding-bottom: 0.3rem;
}

.preview-body h3 {
  font-size: 1.3rem;
}

.preview-body p {
  margin-bottom: 1rem;
}

.preview-body blockquote {
  border-left: 4px solid #f6d55c;
  padding-left: 1rem;
  margin: 1.5rem 0;
  font-style: italic;
  color: #6c757d;
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 0 8px 8px 0;
}

.preview-body code {
  background: #f1f3f4;
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.9rem;
}

.preview-body pre {
  background: #2d3748;
  color: #e2e8f0;
  padding: 1rem;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1.5rem 0;
}

.preview-body pre code {
  background: none;
  padding: 0;
  color: inherit;
}

.preview-body ul,
.preview-body ol {
  margin-bottom: 1rem;
  padding-left: 2rem;
}

.preview-body li {
  margin-bottom: 0.5rem;
}

.preview-body img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  margin: 1rem 0;
}

.preview-body a {
  color: #007bff;
  text-decoration: none;
}

.preview-body a:hover {
  text-decoration: underline;
}

.content-preview {
  background: white;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.form-label {
  font-weight: 600;
  color: #495057;
  margin-bottom: 0.5rem;
}

.form-control-lg {
  font-size: 1.25rem;
  padding: 1rem;
  border-radius: 12px;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
}

.form-control-lg:focus {
  border-color: #f6d55c;
  box-shadow: 0 0 0 0.2rem rgba(246, 213, 92, 0.25);
}

.editor-toolbar-wrapper {
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-bottom: none;
  border-radius: 12px 12px 0 0;
  padding: 0.5rem 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
  position: relative;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.editor-toolbar-wrapper::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, transparent, #e9ecef, transparent);
}

.editor-toolbar {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}

/* 工具栏按钮样式优化 */
.editor-toolbar .btn {
  border-radius: 6px;
  transition: all 0.2s ease;
  border: 1px solid #dee2e6;
  background: white;
  color: #6c757d;
}

.editor-toolbar .btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  border-color: #f6d55c;
  background: #f6d55c;
  color: white;
}

.editor-toolbar .btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

/* 编辑器整体容器效果 */
.content-editor-wrapper {
  position: relative;
  border: 2px solid #e9ecef;
  border-top: none;
  border-radius: 0 0 12px 12px;
  overflow: hidden;
  margin-top: -1px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-editor-wrapper:hover {
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.content-editor-wrapper:focus-within {
  border-color: #f6d55c;
  box-shadow: 0 0 0 0.2rem rgba(246, 213, 92, 0.25);
}

.editor-toolbar-wrapper:focus-within {
  border-color: #f6d55c;
}

.content-editor {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  border: none;
  border-radius: 0;
  transition: all 0.3s ease;
  resize: none;
  height: 100%;
  min-height: 700px;
  overflow-y: auto;
  padding: 1rem;
  background: white;
  flex: 1;
}

.content-editor:focus {
  outline: none;
  box-shadow: 0 0 0 0.2rem rgba(246, 213, 92, 0.25);
}

/* 模态框样式 */
.setting-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e9ecef;
  margin-bottom: 1rem;
}

.setting-title {
  font-size: 1rem;
  font-weight: 600;
  color: #495057;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
}

/* AI摘要生成按钮样式 */
.ai-summary-buttons {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.ai-summary-buttons .btn {
  font-size: 0.875rem;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.ai-summary-buttons .btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.cover-upload {
  text-align: center;
}

.cover-preview {
  width: 100%;
  height: 150px;
  border: 2px dashed #dee2e6;
  border-radius: 12px;
  background-size: cover;
  background-position: center;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-preview:hover {
  border-color: #f6d55c;
  background-color: #f8f9fa;
}

.cover-placeholder {
  color: #6c757d;
  text-align: center;
}

.cover-placeholder i {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  display: block;
}

.tags-input {
  position: relative;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  min-height: 38px;
}

.tag-item {
  background: #f6d55c;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tag-remove {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0;
  font-size: 0.8rem;
}

.tag-remove:hover {
  opacity: 0.8;
}

.publish-confirmation {
  background: white;
  border-radius: 8px;
  padding: 1rem;
}

.confirmation-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.confirmation-list li {
  padding: 0.5rem 0;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  align-items: center;
}

.confirmation-list li:last-child {
  border-bottom: none;
}

.text-success {
  color: #28a745 !important;
}

.text-warning {
  color: #ffc107 !important;
}

.text-danger {
  color: #dc3545 !important;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .publish-header {
    flex-direction: column;
    text-align: center;
    margin-left: 0.5rem;
    margin-right: 0.5rem;
  }
  
  .publish-content {
    margin-left: 0.5rem;
    margin-right: 0.5rem;
  }
  
  .edit-section,
  .preview-section {
    height: 100%;
  }
  
  .preview-section {
    border-left: none;
    border-top: 1px solid #e9ecef;
  }
}

@media (max-width: 768px) {
  .publish-page {
    padding: 0.5rem 0;
  }
  
  .publish-header {
    padding: 1rem;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .edit-section,
  .preview-section {
    padding: 0.75rem;
  }
  
  .preview-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  /* 移动端优化预览区域滚动 */
  .preview-content {
    max-height: 60vh;
    overflow-y: auto;
  }
}
</style>
