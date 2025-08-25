<template>
  <div class="publish-page">
    <div class="container-fluid" style="max-width: 1200px;">
      <!-- 页面头部 -->
      <div class="publish-header">
        <div class="header-content">
          <h1 class="page-title">
            <i class="fas fa-pen-fancy me-3"></i>
            发布新文章
          </h1>
          <p class="page-subtitle">分享您的知识和见解</p>
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
            @click="previewArticle" 
            class="btn btn-outline-info me-2"
            :disabled="!canPreview"
          >
            <i class="fas fa-eye me-1"></i>
            预览
          </button>
          <button 
            @click="publishArticle" 
            class="btn btn-warning"
            :disabled="!canPublish || isPublishing"
          >
            <i class="fas fa-paper-plane me-1"></i>
            {{ isPublishing ? '发布中...' : '发布文章' }}
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

      <!-- 主要内容区域 -->
      <div class="publish-content">
        <div class="row">
          <!-- 左侧编辑区域 -->
          <div class="col-lg-8">
            <div class="edit-section">
              <!-- 文章标题 -->
              <div class="form-group mb-4">
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
                <div class="form-text">
                  <span :class="{ 'text-danger': articleForm.title.length > 100 }">
                    {{ articleForm.title.length }}/100
                  </span>
                </div>
              </div>

              <!-- 文章摘要 -->
              <div class="form-group mb-4">
                <label for="articleExcerpt" class="form-label">
                  <i class="fas fa-align-left me-2"></i>文章摘要
                </label>
                <textarea
                  id="articleExcerpt"
                  v-model="articleForm.excerpt"
                  class="form-control"
                  rows="3"
                  placeholder="请输入文章摘要..."
                  maxlength="200"
                ></textarea>
                <div class="form-text">
                  <span :class="{ 'text-danger': articleForm.excerpt.length > 200 }">
                    {{ articleForm.excerpt.length }}/200
                  </span>
                </div>
              </div>

              <!-- 文章内容 -->
              <div class="form-group mb-4">
                <label for="articleContent" class="form-label">
                  <i class="fas fa-edit me-2"></i>文章内容
                </label>
                <div class="content-editor-wrapper">
                  <textarea
                    id="articleContent"
                    v-model="articleForm.content"
                    class="form-control content-editor"
                    rows="20"
                    placeholder="请输入文章内容，支持Markdown格式..."
                    @input="updateWordCount"
                  ></textarea>
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
                  </div>
                </div>
                <div class="form-text">
                  <span class="text-muted">
                    <i class="fas fa-info-circle me-1"></i>
                    支持Markdown格式，已输入 {{ wordCount }} 字
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧设置区域 -->
          <div class="col-lg-4">
            <div class="settings-section">
              <!-- 封面图片 -->
              <div class="setting-card mb-4">
                <h5 class="setting-title">
                  <i class="fas fa-image me-2"></i>封面图片
                </h5>
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
                      @click="triggerCoverUpload" 
                      class="btn btn-sm btn-outline-primary me-2"
                    >
                      <i class="fas fa-upload me-1"></i>上传
                    </button>
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

              <!-- 文章设置 -->
              <div class="setting-card mb-4">
                <h5 class="setting-title">
                  <i class="fas fa-cog me-2"></i>文章设置
                </h5>
                
                <!-- 标签 -->
                <div class="form-group mb-3">
                  <label class="form-label">标签</label>
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

                <!-- 作品集 -->
                <div class="form-group mb-3">
                  <label class="form-label">作品集</label>
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
                <div class="form-group mb-3">
                  <label class="form-label">发布设置</label>
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
              </div>

              <!-- 发布状态 -->
              <div class="setting-card">
                <h5 class="setting-title">
                  <i class="fas fa-info-circle me-2"></i>发布状态
                </h5>
                <div class="status-info">
                  <div class="status-item">
                    <span class="status-label">标题：</span>
                    <span :class="getStatusClass('title')">
                      {{ getStatusText('title') }}
                    </span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">内容：</span>
                    <span :class="getStatusClass('content')">
                      {{ getStatusText('content') }}
                    </span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">摘要：</span>
                    <span :class="getStatusClass('excerpt')">
                      {{ getStatusText('excerpt') }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 预览模态框 -->
    <div 
      v-if="showPreview" 
      class="modal fade show" 
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">文章预览</h5>
            <button 
              type="button" 
              class="btn-close" 
              @click="showPreview = false"
            ></button>
          </div>
          <div class="modal-body">
            <div class="preview-content">
              <h1>{{ articleForm.title }}</h1>
              <p class="text-muted">{{ articleForm.excerpt }}</p>
              <div v-html="renderedContent"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showPreview" class="modal-backdrop fade show"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { articleAPI, portfolioAPI } from '@/api'
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
const showPreview = ref(false)
const wordCount = ref(0)
const notification = ref({ show: false, message: '', type: 'success' })

// 计算属性
const canPreview = computed(() => articleForm.value.title && articleForm.value.content)
const canPublish = computed(() => {
  return articleForm.value.title.trim() && 
         articleForm.value.content.trim() && 
         articleForm.value.excerpt.trim()
})

const renderedContent = computed(() => {
  return md.render(articleForm.value.content || '')
})

// 方法
const updateWordCount = () => {
  const content = articleForm.value.content || ''
  wordCount.value = content.replace(/\s/g, '').length
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
  if (!value || value.trim() === '') return 'text-danger'
  if (field === 'title' && value.length > 100) return 'text-warning'
  if (field === 'excerpt' && value.length > 200) return 'text-warning'
  return 'text-success'
}

const getStatusText = (field) => {
  const value = articleForm.value[field]
  if (!value || value.trim() === '') return '未填写'
  if (field === 'title' && value.length > 100) return '标题过长'
  if (field === 'excerpt' && value.length > 200) return '摘要过长'
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

const previewArticle = () => {
  showPreview.value = true
}

const publishArticle = async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  try {
    isPublishing.value = true
    
    // 准备发布数据
    const publishData = {
      ...articleForm.value,
      coverImg: coverPreview.value
    }
    
    const response = await articleAPI.publishArticle(publishData)
    
    if (response.success) {
      showNotification('文章发布成功！', 'success')
      // 发布成功，跳转到文章页面
      setTimeout(() => {
        router.push(`/article/${response.slug}`)
      }, 1500)
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
  min-height: 100vh;
  padding: 2rem 0;
}

.publish-header {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.page-title {
  font-size: 2.5rem;
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
  margin-bottom: 2rem;
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
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.edit-section {
  padding-right: 2rem;
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

.content-editor-wrapper {
  position: relative;
}

.content-editor {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  border-radius: 12px;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
  resize: vertical;
}

.content-editor:focus {
  border-color: #f6d55c;
  box-shadow: 0 0 0 0.2rem rgba(246, 213, 92, 0.25);
}

.editor-toolbar {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255,255,255,0.95);
  border-radius: 8px;
  padding: 0.5rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.settings-section {
  position: sticky;
  top: 2rem;
}

.setting-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e9ecef;
}

.setting-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #495057;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
}

.cover-upload {
  text-align: center;
}

.cover-preview {
  width: 100%;
  height: 200px;
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
  font-size: 3rem;
  margin-bottom: 1rem;
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

.status-info {
  font-size: 0.9rem;
}

.status-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid #e9ecef;
}

.status-item:last-child {
  border-bottom: none;
}

.status-label {
  color: #6c757d;
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

.modal-xl {
  max-width: 90%;
}

.preview-content {
  padding: 2rem;
  background: #f8f9fa;
  border-radius: 12px;
}

.preview-content h1 {
  color: #343a40;
  margin-bottom: 1rem;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .publish-header {
    flex-direction: column;
    text-align: center;
  }
  
  .edit-section {
    padding-right: 0;
    margin-bottom: 2rem;
  }
  
  .settings-section {
    position: static;
  }
}

@media (max-width: 768px) {
  .publish-page {
    padding: 1rem 0;
  }
  
  .publish-header,
  .publish-content {
    padding: 1rem;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .header-actions {
    justify-content: center;
  }
}
</style>
