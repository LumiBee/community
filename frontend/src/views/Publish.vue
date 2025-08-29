<template>
  <div class="publish-page">
    <!-- 合并的编辑区域 -->
    <div class="unified-editor-container">
      <!-- 标题部分 -->
      <div class="title-section">
        <i class="fas fa-edit title-icon"></i>
        <input
          type="text"
          v-model="articleForm.title"
          class="header-title-input"
          placeholder="请输入文章标题..."
          maxlength="100"
        />
        <div class="title-actions">
          <button 
            @click="saveDraft" 
            class="btn btn-outline-secondary me-2"
            :disabled="isSaving"
          >
            <i class="fas fa-save me-1"></i>
            {{ isSaving ? '保存中...' : '保存草稿' }}
          </button>
          <button 
            @click="togglePublishModal" 
            class="btn btn-warning"
            :disabled="!canPublish"
          >
            <i class="fas fa-paper-plane me-1"></i>
            {{ isEditMode ? '更新文章' : '发布文章' }}
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

      <!-- 文章内容标签和字数统计 -->
      <div class="editor-header mb-0">
        <div class="editor-header-content">
          <label for="articleContent" class="form-label">
            <i class="fas fa-edit me-2"></i>文章内容
          </label>
          <span class="word-count">
            <i class="fas fa-info-circle me-1"></i>
            {{ wordCount }} 字
          </span>
        </div>
      </div>
      
      <!-- Vditor Markdown编辑器 -->
      <div class="content-editor-wrapper" ref="editorWrapper">
        <div id="vditor" ref="vditorContainer"></div>
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
      style="display: block; z-index: 1050; position: fixed; top: 0; left: 0; right: 0; bottom: 0;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-xl" style="z-index: 1051; margin-top: 6rem; margin-bottom: 2rem;">
        <div class="modal-content publish-settings-modal">
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
                  <div class="setting-header-with-buttons">
                    <h6 class="setting-title">
                      <i class="fas fa-align-left me-2"></i>文章摘要
                      <span class="text-danger ms-2" style="font-size: 0.9rem; font-weight: normal;">(必填)</span>
                    </h6>
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
                  </div>
                  <div class="form-group mb-0">
                    <textarea
                      v-model="articleForm.excerpt"
                      class="form-control excerpt-textarea"
                      rows="2"
                      placeholder="请输入文章摘要...（必填）或点击上方按钮使用AI生成"
                      maxlength="300"
                      :class="{ 'is-invalid': !articleForm.excerpt.trim() }"
                    ></textarea>
                    <div class="form-text">
                      <span :class="{ 'text-danger': articleForm.excerpt.length > 300 || !articleForm.excerpt.trim() }">
                        {{ articleForm.excerpt.length }}/300
                        <span v-if="!articleForm.excerpt.trim()" class="ms-2">摘要不能为空</span>
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
                    <span class="text-muted ms-2" style="font-size: 0.9rem; font-weight: normal;">(可选)</span>
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
                      placeholder="输入标签后按回车...（可选）"
                    />
                  </div>
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
              </div>

              <!-- 右侧设置 -->
              <div class="col-md-6">
                <!-- 作品集 -->
                <div class="setting-card mb-4">
                  <h6 class="setting-title">
                    <i class="fas fa-briefcase me-2"></i>作品集
                    <span class="text-muted ms-2" style="font-size: 0.9rem; font-weight: normal;">(可选)</span>
                  </h6>
                  <div class="portfolio-input-group">
                    <!-- 选择现有作品集 -->
                    <div class="form-group mb-2">
                      <label class="form-label small text-muted">选择现有作品集：</label>
                      <div class="custom-select" @click="togglePortfolioDropdown" ref="portfolioSelectContainer">
                        <div class="select-display">
                          <span class="select-text">{{ getPortfolioSelectText() }}</span>
                          <i class="fas fa-chevron-down select-arrow" :class="{ 'rotated': isPortfolioDropdownOpen }"></i>
                        </div>
                        <div class="select-dropdown" v-show="isPortfolioDropdownOpen">
                          <div 
                            class="select-option"
                            :class="{ 'selected': selectedPortfolioId === '' }"
                            @click.stop="selectPortfolio('')"
                          >
                            -- 选择现有作品集 --
                          </div>
                          <div 
                            v-for="portfolio in portfolios" 
                            :key="portfolio.id"
                            class="select-option"
                            :class="{ 'selected': selectedPortfolioId === portfolio.id }"
                            @click.stop="selectPortfolio(portfolio.id)"
                          >
                            {{ portfolio.title }}
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 或者输入新作品集 -->
                    <div class="form-group">
                      <label class="form-label small text-muted">或者输入新作品集名称：</label>
                      <div class="input-group">
                        <input
                          v-model="newPortfolioName"
                          type="text"
                          class="form-control"
                          placeholder="输入作品集名称..."
                          @keyup.enter="createNewPortfolio"
                          @blur="createNewPortfolio"
                        />
                        <button 
                          type="button" 
                          class="btn btn-outline-primary"
                          @click="createNewPortfolio"
                          :disabled="!newPortfolioName.trim() || isCreatingPortfolio"
                        >
                          <i class="fas" :class="isCreatingPortfolio ? 'fa-spinner fa-spin' : 'fa-plus'"></i>
                        </button>
                      </div>
                    </div>
                    
                    <!-- 当前选中的作品集显示 -->
                    <div v-if="articleForm.portfolioId" class="selected-portfolio mt-2">
                      <div class="alert alert-info py-2">
                        <i class="fas fa-check-circle me-2"></i>
                        已选择作品集：<strong>{{ getSelectedPortfolioName() }}</strong>
                        <button 
                          type="button" 
                          class="btn btn-sm btn-outline-danger ms-2"
                          @click="clearPortfolio"
                        >
                          <i class="fas fa-times"></i>
                        </button>
                      </div>
                    </div>
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
                      <li :class="getStatusClass('portfolio')">
                        <i class="fas fa-briefcase me-2"></i>
                        作品集：{{ getStatusText('portfolio') }}
                      </li>
                      <li :class="getStatusClass('tags')">
                        <i class="fas fa-tags me-2"></i>
                        标签：{{ getStatusText('tags') }}
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
              :disabled="!canActuallyPublish || isPublishing"
            >
              <i class="fas fa-paper-plane me-1"></i>
              {{ isPublishing ? '发布中...' : '确认发布' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showPublishModal" class="modal-backdrop fade show" style="z-index: 1049;"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { articleAPI, portfolioAPI, aiAPI } from '@/api'
import Vditor from 'vditor'
import 'vditor/dist/index.css'

// 确保Vditor CSS和图标字体正确加载
const vditorCSS = document.createElement('link')
vditorCSS.rel = 'stylesheet'
vditorCSS.href = 'https://unpkg.com/vditor@3.11.1/dist/index.css'
document.head.appendChild(vditorCSS)

// 加载Vditor图标字体
const vditorIcons = document.createElement('link')
vditorIcons.rel = 'stylesheet'
vditorIcons.href = 'https://unpkg.com/vditor@3.11.1/dist/css/content-theme/ant.css'
document.head.appendChild(vditorIcons)

// 加载Material Icons字体
const materialIcons = document.createElement('link')
materialIcons.rel = 'stylesheet'
materialIcons.href = 'https://fonts.googleapis.com/icon?family=Material+Icons'
document.head.appendChild(materialIcons)

// 加载Font Awesome字体
const fontAwesome = document.createElement('link')
fontAwesome.rel = 'stylesheet'
fontAwesome.href = 'https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@5.15.4/css/all.min.css'
document.head.appendChild(fontAwesome)

const router = useRouter()
const authStore = useAuthStore()

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
const selectedPortfolioId = ref('')
const newPortfolioName = ref('')
const isSaving = ref(false)
const isPublishing = ref(false)
const isGeneratingSummary = ref(false)
const isCreatingPortfolio = ref(false)
const isPortfolioDropdownOpen = ref(false)
const showPublishModal = ref(false)


// 调试模态框显示状态
const togglePublishModal = () => {
  showPublishModal.value = !showPublishModal.value
}
const wordCount = ref(0)
const notification = ref({ show: false, message: '', type: 'success' })
const portfolioSelectContainer = ref(null)

// 编辑器相关
const editorWrapper = ref(null)
const vditorContainer = ref(null)
let vditor = null

// 编辑模式
const isEditMode = ref(false)
const editingArticleId = ref(null)

// 计算属性
const canPublish = computed(() => {
  return articleForm.value.title.trim() && 
         articleForm.value.content.trim()
})

// 检查是否可以真正发布（包含摘要验证）
const canActuallyPublish = computed(() => {
  return articleForm.value.title.trim() && 
         articleForm.value.content.trim() &&
         articleForm.value.excerpt.trim()
})

// 初始化Vditor编辑器
const initVditor = () => {
  try {
    // 确保DOM元素存在
    const vditorElement = document.getElementById('vditor')
    if (!vditorElement) {
      console.error('Vditor容器元素不存在')
      return
    }
    
    if (vditor) {
      vditor.destroy()
    }
    
    // 动态计算编辑器高度，基于真实DOM元素
    const calculateEditorHeight = () => {
      // 获取当前窗口高度
      const windowHeight = window.innerHeight
      
      // 获取页面顶部到编辑器容器的距离
      const getEditorTopOffset = () => {
        const editorContainer = document.querySelector('.content-editor-wrapper')
        if (!editorContainer) return 200 // 默认值
        
        const rect = editorContainer.getBoundingClientRect()
        return rect.top
      }
      
      // 计算底部边距
      const bottomMargin = 30
      
      // 动态计算可用高度 = 窗口高度 - 编辑器顶部偏移 - 底部边距
      const availableHeight = windowHeight - getEditorTopOffset() - bottomMargin
      
      // 确保至少有最小高度
      return Math.max(500, availableHeight)
    }
    
    vditor = new Vditor('vditor', {
      height: calculateEditorHeight(),
      mode: 'ir', // 即时渲染模式，类似Typora
      placeholder: '请输入文章内容，支持Markdown语法...',
      toolbar: [
        'bold', 'italic', 'strike', 'link', 
        '|', 'headings', 'list', 'ordered-list', 'check',
        '|', 'quote', 'line', 'code', 'inline-code',
        '|', 'upload', 'table',
        '|', 'undo', 'redo',
        '|', 'edit-mode', 'fullscreen',
        '|', 'preview'
      ],
      typewriterMode: false, // 关闭打字机模式，保持左对齐
      outline: {
        enable: false, // 禁用大纲功能
        position: 'left' 
      },
      upload: {
        handler: (files) => {
          // 处理图片上传
          const promises = files.map(file => {
            return new Promise((resolve) => {
              const reader = new FileReader()
              reader.onload = (e) => {
                resolve({
                  msg: '上传成功',
                  code: 0,
                  data: {
                    errFiles: [],
                    succMap: {
                      [file.name]: e.target.result
                    }
                  }
                })
              }
              reader.readAsDataURL(file)
            })
          })
          return Promise.all(promises)
        }
      },
      cache: {
        enable: false
      },
      preview: {
        delay: 1000,
        show: false
      },
      counter: {
        enable: false
      },
      theme: 'classic',
      icon: 'material',
      customRenders: {
        // 自定义渲染器，强制左对齐
        render: (node, entering) => {
          if (entering && node.type === 'paragraph') {
            node.style = node.style || {}
            node.style.textAlign = 'left'
          }
          return true
        }
      },
      customIcons: {},
      classes: {
        preview: 'vditor-reset',
      },
      tab: '  ', // 使用两个空格作为缩进
      after: () => {
        // 检查工具栏是否存在
        const toolbar = document.querySelector('#vditor .vditor-toolbar')
        
        // 强制显示工具栏
        if (toolbar) {
          toolbar.style.display = 'flex'
          toolbar.style.visibility = 'visible'
          toolbar.style.opacity = '1'
          
          // 强制显示所有工具栏项
          const toolbarItems = toolbar.querySelectorAll('.vditor-toolbar__item')
          toolbarItems.forEach(item => {
            item.style.display = 'flex'
            item.style.visibility = 'visible'
            item.style.opacity = '1'
            
            // 检查并修复按钮图标
            const buttons = item.querySelectorAll('button')
            buttons.forEach(button => {
              button.style.display = 'inline-flex'
              button.style.alignItems = 'center'
              button.style.justifyContent = 'center'
              button.style.minWidth = '32px'
              button.style.minHeight = '32px'
              button.style.padding = '8px'
              
              // 检查是否有图标内容
              const iconContent = button.querySelector('svg, .anticon, [class*="icon"], .material-icons')
              if (!iconContent) {
                // 根据按钮的data-type或data-tag属性添加对应的图标
                const buttonType = button.getAttribute('data-type') || 
                                  button.getAttribute('data-tag') || 
                                  button.getAttribute('data-mode')
                
                if (buttonType) {
                  // 为不同类型的按钮添加Material Icons图标
                  let iconName = ''
                  
                  // 根据按钮类型设置图标
                  switch(buttonType) {
                    case 'h1': iconName = 'looks_one'; break;
                    case 'h2': iconName = 'looks_two'; break;
                    case 'h3': iconName = 'looks_3'; break;
                    case 'h4': iconName = 'looks_4'; break;
                    case 'h5': iconName = 'looks_5'; break;
                    case 'h6': iconName = 'looks_6'; break;
                    case 'wysiwyg': iconName = 'visibility'; break;
                    case 'ir': iconName = 'edit'; break;
                    case 'sv': iconName = 'code'; break;
                    default: iconName = 'text_fields';
                  }
                  
                  // 添加Material Icons图标
                  button.innerHTML = `<i class="material-icons" style="font-size: 18px;">${iconName}</i>`
                } else {
                  // 如果无法确定按钮类型，则使用按钮标题作为文本
                  const buttonText = button.getAttribute('title') || button.textContent || ''
                  if (buttonText) {
                    button.innerHTML = `<span>${buttonText}</span>`
                  }
                }
              }
            })
          })
          
          // 延迟添加文本标签和检查图标
          setTimeout(() => {
            const buttons = toolbar.querySelectorAll('button')
            buttons.forEach((button, index) => {
              // 检查按钮是否有任何内容
              if (button.innerHTML.trim() === '') {
                const buttonType = button.getAttribute('data-type') || 
                                  button.getAttribute('data-tag') || 
                                  button.getAttribute('data-mode')
                
                // 根据按钮类型设置图标
                let iconName = ''
                switch(buttonType) {
                  case 'h1': iconName = 'looks_one'; break;
                  case 'h2': iconName = 'looks_two'; break;
                  case 'h3': iconName = 'looks_3'; break;
                  case 'h4': iconName = 'looks_4'; break;
                  case 'h5': iconName = 'looks_5'; break;
                  case 'h6': iconName = 'looks_6'; break;
                  case 'wysiwyg': iconName = 'visibility'; break;
                  case 'ir': iconName = 'edit'; break;
                  case 'sv': iconName = 'code'; break;
                  default: iconName = 'text_fields';
                }
                
                // 添加Material Icons图标
                button.innerHTML = `<i class="material-icons" style="font-size: 18px;">${iconName}</i>`
              }
              
              // 为按钮添加title提示
              if (!button.title) {
                const buttonType = button.getAttribute('data-type') || 
                                  button.getAttribute('data-tag') || 
                                  button.getAttribute('data-mode')
                if (buttonType) {
                  button.title = buttonType.charAt(0).toUpperCase() + buttonType.slice(1)
                }
              }
            })
            
            // 再次检查是否有按钮缺少图标
            const emptyButtons = Array.from(buttons).filter(btn => btn.innerHTML.trim() === '')
          }, 500)
        }
        
        // 编辑器初始化完成后的回调
        if (articleForm.value.content) {
          vditor.setValue(articleForm.value.content)
        }
        
        // 监听内容变化 - 使用正确的API
        try {
          // 使用Vditor 3.x的正确事件监听方式
          if (vditor && vditor.vditor) {
            // 设置内容变化的回调函数
            const originalSetValue = vditor.setValue.bind(vditor)
            vditor.setValue = (value) => {
              originalSetValue(value)
              articleForm.value.content = value
              updateWordCount()
            }
            
            // 定期检查内容变化
            const contentCheckInterval = setInterval(() => {
              if (vditor && typeof vditor.getValue === 'function') {
                const currentContent = vditor.getValue()
                if (currentContent !== articleForm.value.content) {
                  articleForm.value.content = currentContent
                  updateWordCount()
                }
              } else {
                clearInterval(contentCheckInterval)
              }
            }, 1000)
            
            // 存储定时器引用，在组件卸载时清除
            const contentCheckIntervalRef = contentCheckInterval
          }
        } catch (error) {
          console.warn('Vditor事件监听器设置失败:', error)
        }
        
        // 延迟检查工具栏状态
        setTimeout(() => {
          const toolbarAfter = document.querySelector('#vditor .vditor-toolbar')
          
          // 强制设置编辑器左对齐
          const editorElements = document.querySelectorAll('#vditor .vditor-ir, #vditor .vditor-sv, #vditor .vditor-wysiwyg')
          editorElements.forEach(element => {
            element.style.textAlign = 'left'
            element.style.paddingLeft = '0'
            element.style.marginLeft = '0'
            
            // 设置内部编辑器元素，留一点左侧空间
            const innerEditor = element.querySelector('.vditor-ir__editor, .vditor-sv__editor, .vditor-wysiwyg__editor')
            if (innerEditor) {
              innerEditor.style.textAlign = 'left'
              innerEditor.style.paddingLeft = '20px'
              innerEditor.style.marginLeft = '0'
            }
          })
          
          // 简化样式设置，避免性能问题
          const applyEditorStyles = () => {
            // 只设置必要的样式，避免过度操作
            const editorSelectors = [
              '#vditor .vditor-ir__editor',
              '#vditor .vditor-sv__editor',
              '#vditor .vditor-wysiwyg__editor'
            ]
            
            editorSelectors.forEach(selector => {
              const elements = document.querySelectorAll(selector)
              elements.forEach(element => {
                // 只在需要时设置样式，避免重复设置
                if (element.style.textAlign !== 'left') {
                  element.style.setProperty('text-align', 'left', 'important')
                }
                if (element.style.fontSize !== '16px') {
                  element.style.setProperty('font-size', '16px', 'important')
                }
                if (element.style.lineHeight !== '1.8') {
                  element.style.setProperty('line-height', '1.8', 'important')
                }
              })
            })
          }
          
          // 只在初始化时执行一次
          applyEditorStyles()
          
          // 延迟执行多次，确保样式生效
          setTimeout(applyEditorStyles, 1000)
          setTimeout(applyEditorStyles, 2000)
          setTimeout(applyEditorStyles, 3000)
          

          
          // 简化的样式调整功能
          const adjustEditorStyles = (options = {}) => {
            const {
              fontSize = '16px',
              lineHeight = '1.8'
            } = options
            
            const editorSelectors = [
              '#vditor .vditor-ir__editor',
              '#vditor .vditor-sv__editor', 
              '#vditor .vditor-wysiwyg__editor'
            ]
            
            editorSelectors.forEach(selector => {
              const elements = document.querySelectorAll(selector)
              elements.forEach(element => {
                element.style.setProperty('font-size', fontSize, 'important')
                element.style.setProperty('line-height', lineHeight, 'important')
              })
            })
          }
          
          // 将调整函数暴露到全局，方便调试
          window.adjustEditorStyles = adjustEditorStyles
          
          // 监听窗口大小变化，自动调整编辑器高度
          const handleResize = () => {
            if (vditor) {
              // 延迟执行，确保DOM已更新
              setTimeout(() => {
                const newHeight = calculateEditorHeight()
                vditor.setHeight(newHeight)
              }, 100)
            }
          }
          
          // 添加窗口大小变化监听器
          window.addEventListener('resize', handleResize)
          
          // 存储监听器引用，在组件卸载时移除
          const handleResizeRef = handleResize
        }, 1000)
      }
    })
    
  } catch (error) {
    console.error('Vditor初始化失败:', error)
  }
}

// 方法
const updateWordCount = () => {
  const content = articleForm.value.content || ''
  // 对于HTML内容，需要去除HTML标签来计算字数
  const plainText = content.replace(/<[^>]*>/g, '').replace(/\s/g, '')
  wordCount.value = plainText.length
}

// Vditor编辑器工具栏方法（Vditor内置工具栏，无需额外方法）
// 触发图片上传
const triggerImageUpload = () => {
  imageUploadInput.value.click()
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
      // 在Vditor编辑器中插入图片
      if (vditor) {
        try {
          // 使用Vditor的API插入图片
          const imageMarkdown = `![${file.name}](${e.target.result})`
          let currentValue = ''
          
          // 获取当前内容
          if (typeof vditor.getValue === 'function') {
            currentValue = vditor.getValue()
          } else if (vditor.vditor && typeof vditor.vditor.getValue === 'function') {
            currentValue = vditor.vditor.getValue()
          }
          
          const newValue = currentValue + '\n' + imageMarkdown + '\n'
          
          // 设置新内容
          if (typeof vditor.setValue === 'function') {
            vditor.setValue(newValue)
          } else if (vditor.vditor && typeof vditor.vditor.setValue === 'function') {
            vditor.vditor.setValue(newValue)
          } else if (vditor.vditor && vditor.vditor.lute) {
            vditor.vditor.lute.WYSIWYGSetContent(newValue)
          }
          
          // 更新表单内容
          articleForm.value.content = newValue
          updateWordCount()
        } catch (error) {
          console.warn('在Vditor中插入图片失败:', error)
          // 备用方案：直接更新表单内容
          const imageMarkdown = `![${file.name}](${e.target.result})`
          articleForm.value.content += '\n' + imageMarkdown + '\n'
          updateWordCount()
        }
      }
      
      isImageUploading.value = false
      showNotification('图片已插入到文章中', 'success')
    }
    reader.onerror = () => {
      isImageUploading.value = false
      showNotification('图片上传失败，请重试', 'danger')
    }
    reader.readAsDataURL(file)
  }
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
  if (field === 'excerpt') {
    const value = articleForm.value[field]
    // 摘要字段在发布设置中是必填的
    if (!value || value.trim() === '') return 'text-danger'
    if (value.length > 300) return 'text-warning'
    return 'text-success'
  }
  if (field === 'portfolio') {
    // 作品集字段是可选的，检查portfolioId
    const portfolioId = articleForm.value.portfolioId
    return portfolioId ? 'text-success' : 'text-muted'
  }
  if (field === 'tags') {
    // 标签字段是可选的，空值不显示错误
    return articleForm.value.tags.length > 0 ? 'text-success' : 'text-muted'
  }
  
  const value = articleForm.value[field]
  if (!value || value.trim() === '') return 'text-danger'
  if (field === 'title' && value.length > 100) return 'text-warning'
  return 'text-success'
}

const getStatusText = (field) => {
  if (field === 'excerpt') {
    const value = articleForm.value[field]
    // 摘要字段在发布设置中是必填的
    if (!value || value.trim() === '') return '未填写（必填）'
    if (value.length > 300) return '摘要过长'
    return '已填写'
  }
  if (field === 'portfolio') {
    // 作品集字段是可选的，检查portfolioId
    const portfolioId = articleForm.value.portfolioId
    if (!portfolioId) return '未选择（可选）'
    const portfolio = portfolios.value.find(p => p.id === portfolioId)
    return portfolio ? portfolio.title : '未知作品集'
  }
  if (field === 'tags') {
    // 标签字段是可选的
    if (articleForm.value.tags.length === 0) return '未添加（可选）'
    return articleForm.value.tags.join(', ')
  }
  
  const value = articleForm.value[field]
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
    // 准备草稿数据，确保字段名与后端DTO匹配
    const draftData = {
      title: articleForm.value.title,
      content: articleForm.value.content,
      excerpt: articleForm.value.excerpt,
      tagsName: articleForm.value.tags,
      portfolioName: articleForm.value.portfolioId ? 
        portfolios.value.find(p => p.id === articleForm.value.portfolioId)?.title : null
    }
    
    const response = await articleAPI.saveDraft(draftData)
    
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
    // 在发布前检查token有效性
    try {
      await authStore.refreshToken()
    } catch (refreshError) {
      // Token刷新失败时继续使用当前token
    }
    
    // 调试：检查发布条件
    console.log('发布条件检查:', {
      title: articleForm.value.title,
      content: articleForm.value.content,
      excerpt: articleForm.value.excerpt,
      canActuallyPublish: canActuallyPublish.value
    })
    
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
    
    // 准备发布数据，确保字段名与后端DTO匹配
    const publishData = {
      title: articleForm.value.title,
      content: articleForm.value.content,
      excerpt: finalExcerpt,
      tagsName: articleForm.value.tags,  // 转换为后端期望的字段名
      portfolioName: articleForm.value.portfolioId ? 
        portfolios.value.find(p => p.id === articleForm.value.portfolioId)?.title : null,  // 转换为作品集名称
      coverImg: coverPreview.value
    }
    
    let response
    if (isEditMode.value) {
      // 编辑模式：更新文章
      response = await articleAPI.updateArticle(editingArticleId.value, publishData)
      if (response && response.articleId) {
        showNotification('文章更新成功！', 'success')
        showPublishModal.value = false
        // 更新成功，跳转到文章页面
        setTimeout(() => {
          router.push(`/article/${response.slug}`)
        }, 200)
      } else {
        showNotification('更新失败，请重试', 'danger')
      }
    } else {
      // 发布模式：发布新文章
      response = await articleAPI.publishArticle(publishData)
      
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
    }
  } catch (error) {
    console.error('发布文章失败:', error)
    showNotification('发布失败：' + (error.message || '未知错误'), 'danger')
  } finally {
    isPublishing.value = false
  }
}

// 作品集相关方法
const togglePortfolioDropdown = () => {
  isPortfolioDropdownOpen.value = !isPortfolioDropdownOpen.value
}

const selectPortfolio = (portfolioId) => {
  selectedPortfolioId.value = portfolioId
  if (portfolioId) {
    articleForm.value.portfolioId = portfolioId
    newPortfolioName.value = '' // 清空新作品集输入
  } else {
    articleForm.value.portfolioId = ''
  }
  isPortfolioDropdownOpen.value = false
}

const getPortfolioSelectText = () => {
  
  if (!selectedPortfolioId.value) {
    return '-- 选择现有作品集 --'
  }
  const portfolio = portfolios.value.find(p => p.id === selectedPortfolioId.value)
  return portfolio ? portfolio.title : '-- 选择现有作品集 --'
}

const createNewPortfolio = async () => {
  const portfolioName = newPortfolioName.value.trim()
  if (!portfolioName) return
  
  try {
    isCreatingPortfolio.value = true
    
    // 创建新作品集
    const newPortfolio = {
      title: portfolioName,
      description: `由文章《${articleForm.value.title || '未命名文章'}》创建的作品集`
    }
    
    const response = await portfolioAPI.createPortfolio(newPortfolio)
    
    if (response && response.id) {
      // 转换后端返回的Portfolio对象为前端期望的格式
      const portfolioForFrontend = {
        id: response.id,
        title: response.name, // 后端返回的是name，前端期望的是title
        description: response.description,
        userId: response.userId,
        slug: response.slug,
        coverImgUrl: response.coverImgUrl,
        gmtCreate: response.gmtModified, // 使用gmtModified作为gmtCreate
        gmtModified: response.gmtModified
      }
      
      // 添加到作品集列表
      portfolios.value.push(portfolioForFrontend)
      
      // 设置为当前选中的作品集
      selectedPortfolioId.value = response.id
      articleForm.value.portfolioId = response.id
      
      // 清空输入
      newPortfolioName.value = ''
      
      showNotification(`作品集"${portfolioName}"创建成功！`, 'success')
    } else {
      throw new Error(`创建作品集失败: ${response?.message || '响应格式不正确'}`)
    }
  } catch (error) {
    console.error('创建作品集失败:', error)
    showNotification('创建作品集失败：' + (error.message || '未知错误'), 'danger')
  } finally {
    isCreatingPortfolio.value = false
  }
}

const getSelectedPortfolioName = () => {

  if (!articleForm.value.portfolioId) return ''
  
  const portfolio = portfolios.value.find(p => p.id === articleForm.value.portfolioId)
  return portfolio ? portfolio.title : '未知作品集'
}

const clearPortfolio = () => {
  articleForm.value.portfolioId = ''
  selectedPortfolioId.value = ''
  newPortfolioName.value = ''
}

const loadPortfolios = async () => {
  try {
    const response = await portfolioAPI.getAllPortfolios()    
    // 转换后端返回的PortfolioDetailsDTO为前端期望的格式
    portfolios.value = (response || []).map(portfolio => ({
      id: portfolio.id,
      title: portfolio.name, // 后端返回的是name，前端期望的是title
      description: portfolio.description,
      userId: portfolio.userId,
      slug: portfolio.slug,
      coverImgUrl: portfolio.coverImgUrl,
      gmtCreate: portfolio.gmtModified, // 使用gmtModified作为gmtCreate
      gmtModified: portfolio.gmtModified
    }))
    
  } catch (error) {
    console.error('加载作品集失败:', error)
  }
}

// AI生成摘要方法
const generateAISummary = async () => {
  if (!articleForm.value.content || articleForm.value.content.trim() === '') {
    showNotification('请先输入文章内容', 'warning')
    return
  }

  try {
    isGeneratingSummary.value = true
    showNotification('正在使用AI生成摘要...', 'info')
        
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

// 点击外部关闭下拉框
const handleClickOutside = (event) => {
  if (portfolioSelectContainer.value && !portfolioSelectContainer.value.contains(event.target)) {
    isPortfolioDropdownOpen.value = false
  }
}

// 组件挂载
onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  
  // 等待DOM完全渲染后再初始化Vditor
  await nextTick()
  
  // 初始化Vditor编辑器
  initVditor()
  
  // 检查是否是编辑模式
  const route = useRoute()
  if (route && route.query && route.query.edit) {
    const editId = route.query.edit
    isEditMode.value = true
    editingArticleId.value = parseInt(editId)
    loadArticleForEdit(editingArticleId.value)
  }
  
  loadPortfolios()
  
  // 添加点击外部关闭下拉框的监听器
  document.addEventListener('click', handleClickOutside)
  
  // 确保字数统计从0开始
  updateWordCount()
})

// 加载文章用于编辑
const loadArticleForEdit = async (articleId) => {
  try {
    const article = await articleAPI.getArticleById(articleId)
    if (article) {
      // 填充表单数据
      articleForm.value = {
        title: article.title || '',
        excerpt: article.excerpt || '',
        content: article.content || '',
        tags: article.tags ? article.tags.map(tag => tag.name) : [],
        portfolioId: article.portfolioId || '',
        allowComments: article.allowComments !== false
      }
      
      // 设置封面图片
      if (article.coverImg) {
        coverPreview.value = article.coverImg
      }
      
      // 设置作品集
      if (article.portfolioId) {
        selectedPortfolioId.value = article.portfolioId
      }
      
      // 设置编辑器内容
      if (vditor && article.content) {
        try {
          if (typeof vditor.setValue === 'function') {
            vditor.setValue(article.content)
          } else if (vditor.vditor && typeof vditor.vditor.setValue === 'function') {
            vditor.vditor.setValue(article.content)
          } else if (vditor.vditor && vditor.vditor.lute) {
            // 直接设置内容
            vditor.vditor.lute.WYSIWYGSetContent(article.content)
          }
        } catch (error) {
          console.warn('设置Vditor内容失败:', error)
        }
      }
      
      // 更新字数统计
      updateWordCount()
      
      showNotification('文章加载成功，可以开始编辑', 'success')
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    showNotification('加载文章失败：' + (error.message || '未知错误'), 'danger')
  }
}

// 组件卸载时移除监听器
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  
  // 清理定时器和事件监听器
  if (window.contentCheckIntervalRef) {
    clearInterval(window.contentCheckIntervalRef)
  }
  if (window.handleResizeRef) {
    window.removeEventListener('resize', window.handleResizeRef)
  }
  
  // 销毁Vditor编辑器
  if (vditor) {
    vditor.destroy()
    vditor = null
  }
})
</script>

<style scoped>
.publish-page {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  height: calc(100vh - 80px);
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: fixed;
  top: 80px;
  left: 0;
  right: 0;
  bottom: 0;
}



/* 合并的编辑器容器 */
.unified-editor-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  margin: 1rem auto;
  max-width: 1500px;
  width: 95%;
  border: 1px solid #e9ecef;
  padding: 1.5rem;
}

/* 标题部分样式 */
.title-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.25rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e9ecef;
  height: 60px; /* 固定高度，与计算函数保持一致 */
  box-sizing: border-box;
}

.title-icon {
  font-size: 1.25rem;
  color: #6c757d;
  margin-right: 0.5rem;
}

.title-actions {
  display: flex;
  gap: 0.75rem;
  flex-shrink: 0;
}

/* 头部标题输入框样式 */
.header-title-input {
  border: none;
  background: transparent;
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c3e50;
  padding: 0.5rem 0;
  margin: 0;
  flex: 1;
  min-width: 300px;
  outline: none;
  transition: all 0.2s ease;
  line-height: 1.3;
  letter-spacing: 0.3px;
}

.header-title-input:focus {
  border: none;
  outline: none;
  box-shadow: none;
  color: #34495e;
  transform: translateY(-2px);
}

.header-title-input:hover {
  color: #34495e;
  transform: translateY(-1px);
}

.header-title-input::placeholder {
  color: #bdc3c7;
  font-weight: 400;
  font-size: 1.2rem;
  opacity: 0.8;
  transition: all 0.3s ease;
}

.header-title-input:focus::placeholder {
  opacity: 0.5;
  transform: translateX(5px);
}

.header-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.header-actions .btn {
  border-radius: 6px;
  padding: 0.5rem 1.25rem;
  font-weight: 500;
  font-size: 0.9rem;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

.header-actions .btn:hover {
  transform: none;
  box-shadow: 0 2px 6px rgba(0,0,0,0.12);
}

.header-actions .btn-outline-secondary {
  border: 1px solid #95a5a6;
  color: #7f8c8d;
  background: white;
}

.header-actions .btn-outline-secondary:hover {
  border-color: #f6d55c;
  background: #f6d55c;
  color: white;
}

.header-actions .btn-warning {
  background: #f39c12;
  border: none;
  color: white;
}

.header-actions .btn-warning:hover {
  background: #e67e22;
  transform: none;
}

/* 通知样式 */
.alert {
  border-radius: 6px;
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  margin: 0 0 1rem 0;
  flex-shrink: 0;
  padding: 0.5rem 1rem;
  min-height: 45px;
}

.alert-success {
  background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
  color: #155724;
  border-left: 4px solid #28a745;
}

.alert-danger {
  background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%);
  color: #721c24;
  border-left: 4px solid #dc3545;
}

.alert-warning {
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
  color: #856404;
  border-left: 4px solid #ffc107;
}







.editor-header {
  flex-shrink: 0;
  padding: 0.25rem 0;
  margin-bottom: 0.5rem;
  height: 40px; /* 固定高度，与计算函数保持一致 */
  box-sizing: border-box;
}

.editor-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: transparent;
  padding: 0.2rem 0 0.2rem 0.6rem;
  border-radius: 0;
  border: none;
}

.editor-header .form-label {
  font-size: 1rem;
  font-weight: 600;
  color: #495057;
  margin: 0;
  display: flex;
  align-items: center;
}

.editor-header .form-label i {
  color: #6c757d;
  margin-right: 0.5rem;
}

.word-count {
  font-size: 0.85rem;
  color: #6c757d;
  background: transparent;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  border: 1px solid #dee2e6;
  box-shadow: none;
  font-weight: 500;
}

.word-count i {
  color: #6c757d;
  margin-right: 0.25rem;
}



/* 编辑器整体容器效果 */
.content-editor-wrapper {
  position: relative;
  overflow: hidden;
  margin-top: 0;
  box-shadow: none;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  background: transparent;
}



/* Vditor编辑器样式 */
#vditor {
  height: auto;
  min-height: 500px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  width: 100%;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  overflow: hidden;
  transition: height 0.3s ease;
}

#vditor .vditor-ir,
#vditor .vditor-sv,
#vditor .vditor-wysiwyg {
  overflow: hidden !important;
}

#vditor .vditor-ir__editor,
#vditor .vditor-sv__editor,
#vditor .vditor-wysiwyg__editor {
  overflow: hidden !important;
}

/* 确保Vditor编辑器在容器中正确显示 */
.content-editor-wrapper {
  border: none;
  box-shadow: none;
  background: transparent;
  width: 100%;
}

/* 修复Vditor工具栏显示问题 */
#vditor .vditor-toolbar {
  display: flex !important;
  visibility: visible !important;
  opacity: 1 !important;
  position: relative !important;
  z-index: 10 !important;
}

#vditor .vditor-toolbar .vditor-toolbar__item {
  display: flex !important;
  visibility: visible !important;
  opacity: 1 !important;
  position: relative !important;
}

/* 编辑器内容完全左对齐 */
#vditor .vditor-ir {
  text-align: left !important;
  padding-left: 0 !important;
  padding-right: 0 !important;
  max-width: none !important;
  margin: 0 !important;
}

#vditor .vditor-sv {
  text-align: left !important;
  padding-left: 0 !important;
  padding-right: 0 !important;
  max-width: none !important;
  margin: 0 !important;
}

#vditor .vditor-wysiwyg {
  text-align: left !important;
  padding-left: 0 !important;
  padding-right: 0 !important;
  max-width: none !important;
  margin: 0 !important;
}

/* 设置文本框内容左对齐 */
#vditor [contenteditable="true"] {
  text-align: left !important;
}

/* 确保所有文本元素左对齐 */
#vditor .vditor-reset {
  text-align: left !important;
}

/* 文章段落左对齐 */
#vditor .vditor-reset p {
  text-align: left !important;
  margin-left: 0 !important;
}

/* 标题左对齐 */
#vditor .vditor-reset h1,
#vditor .vditor-reset h2,
#vditor .vditor-reset h3,
#vditor .vditor-reset h4,
#vditor .vditor-reset h5,
#vditor .vditor-reset h6 {
  text-align: left !important;
  margin-left: 0 !important;
}

/* 编辑器输入区域完全左对齐 */
#vditor .vditor-ir__editor,
#vditor .vditor-sv__editor,
#vditor .vditor-wysiwyg__editor {
  text-align: left !important;
  padding-left: 0 !important;
  margin-left: 0 !important;
}

/* 确保编辑器内容区域左对齐 */
#vditor .vditor-content {
  text-align: left !important;
}

/* 编辑器工具栏左对齐 */
#vditor .vditor-toolbar {
  justify-content: flex-start !important;
}

/* 强制所有编辑器内容左对齐 */
#vditor .vditor-ir,
#vditor .vditor-sv,
#vditor .vditor-wysiwyg {
  text-align: left !important;
}

/* 编辑器内部所有元素左对齐 */
#vditor .vditor-ir *,
#vditor .vditor-sv *,
#vditor .vditor-wysiwyg * {
  text-align: left !important;
}

/* 编辑器容器左对齐 */
#vditor .vditor {
  text-align: left !important;
}

/* 编辑器输入框左对齐 */
#vditor textarea,
#vditor [contenteditable="true"] {
  text-align: left !important;
  padding-left: 0 !important;
  margin-left: 0 !important;
}

/* 编辑器占位符左对齐 */
#vditor .vditor-ir__editor::placeholder,
#vditor .vditor-sv__editor::placeholder,
#vditor .vditor-wysiwyg__editor::placeholder {
  text-align: left !important;
}

/* 使用更强的选择器覆盖Vditor默认样式 */
#vditor .vditor-ir,
#vditor .vditor-sv,
#vditor .vditor-wysiwyg,
#vditor .vditor-content,
#vditor .vditor-reset,
#vditor [contenteditable="true"],
#vditor textarea,
#vditor .vditor-ir *,
#vditor .vditor-sv *,
#vditor .vditor-wysiwyg * {
  text-align: left !important;
  text-align-last: left !important;
  padding-left: 0 !important;
  margin-left: 0 !important;
}

/* 编辑器输入区域字体设置 */
#vditor .vditor-ir__editor,
#vditor .vditor-sv__editor,
#vditor .vditor-wysiwyg__editor {
  text-align: left !important;
  text-align-last: left !important;
  margin-left: 0 !important;
  font-size: 16px !important;
  line-height: 1.8 !important;
}

/* 强制覆盖Vditor的居中样式 */
#vditor .vditor-ir[style*="text-align: center"],
#vditor .vditor-sv[style*="text-align: center"],
#vditor .vditor-wysiwyg[style*="text-align: center"] {
  text-align: left !important;
}

/* 使用CSS变量强制设置 */
#vditor {
  --vditor-text-align: left !important;
}

/* 覆盖可能的flex居中 */
#vditor .vditor-ir,
#vditor .vditor-sv,
#vditor .vditor-wysiwyg {
  justify-content: flex-start !important;
  align-items: flex-start !important;
}

#vditor .vditor-toolbar .vditor-toolbar__item button {
  display: inline-flex !important;
  visibility: visible !important;
  opacity: 1 !important;
}

/* 确保Vditor内容区域正确显示 */
#vditor .vditor-content {
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

/* 强制显示所有Vditor元素 */
#vditor * {
  visibility: visible !important;
}

#vditor .vditor-toolbar,
#vditor .vditor-toolbar * {
  display: flex !important;
  visibility: visible !important;
  opacity: 1 !important;
}

/* 修复图标显示问题 */
#vditor .vditor-toolbar .vditor-toolbar__item button::before {
  font-family: 'Material Icons', 'FontAwesome', sans-serif !important;
  font-size: 16px !important;
  line-height: 1 !important;
  display: inline-block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

#vditor .vditor-toolbar .vditor-toolbar__item button svg {
  display: inline-block !important;
  visibility: visible !important;
  opacity: 1 !important;
  width: 16px !important;
  height: 16px !important;
}

/* 确保工具栏按钮内容可见 */
#vditor .vditor-toolbar .vditor-toolbar__item button {
  min-width: 32px !important;
  min-height: 32px !important;
  padding: 8px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
}



.hidden {
  display: none !important;
}





/* 模态框样式 */
.setting-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 16px;
  padding: 1.5rem;
  border: 1px solid #e9ecef;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 16px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}

.setting-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(0,0,0,0.1);
}

.setting-title {
  font-size: 1rem;
  font-weight: 600;
  color: #495057;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
}

.setting-title i {
  color: #f6d55c;
  margin-right: 0.75rem;
}

/* AI摘要生成按钮样式 */
.ai-summary-buttons {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.ai-summary-buttons .btn {
  font-size: 0.875rem;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.ai-summary-buttons .btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.cover-upload {
  text-align: center;
}

.cover-preview {
  width: 100%;
  height: 140px;
  border: 3px dashed #dee2e6;
  border-radius: 12px;
  background-size: cover;
  background-position: center;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(0,0,0,0.05);
}

.cover-preview:hover {
  border-color: #f6d55c;
  background-color: #f8f9fa;
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(0,0,0,0.1);
}

.cover-placeholder {
  color: #6c757d;
  text-align: center;
}

.cover-placeholder i {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
  display: block;
  color: #f6d55c;
}

.tags-input {
  position: relative;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  min-height: 45px;
}

.tag-item {
  background: linear-gradient(135deg, #f6d55c, #f3a712);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 25px;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 2px 8px rgba(246, 213, 92, 0.3);
  transition: all 0.3s ease;
}

.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(246, 213, 92, 0.4);
}

.tag-remove {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0;
  font-size: 0.8rem;
  transition: all 0.3s ease;
}

.tag-remove:hover {
  opacity: 0.8;
  transform: scale(1.1);
}

/* 作品集输入组件样式 */
.portfolio-input-group {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e9ecef;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.portfolio-input-group .form-label {
  font-size: 0.9rem;
  color: #6c757d;
  margin-bottom: 0.75rem;
  font-weight: 500;
}

.portfolio-input-group .form-select,
.portfolio-input-group .form-control {
  border-radius: 8px;
  border: 2px solid #dee2e6;
  transition: all 0.3s ease;
  padding: 0.75rem;
}

.portfolio-input-group .form-select:focus,
.portfolio-input-group .form-control:focus {
  border-color: #f6d55c;
  box-shadow: 0 0 0 0.2rem rgba(246, 213, 92, 0.25);
}

.portfolio-input-group .input-group .btn {
  border-radius: 0 8px 8px 0;
  border-left: none;
  padding: 0.75rem 1rem;
}

.portfolio-input-group .input-group .form-control {
  border-radius: 8px 0 0 8px;
}

.selected-portfolio .alert {
  border-radius: 8px;
  border: 1px solid #bee5eb;
  background: linear-gradient(135deg, #d1ecf1 0%, #cce7f0 100%);
  color: #0c5460;
  margin: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.selected-portfolio .btn-outline-danger {
  border-radius: 6px;
  padding: 0.375rem 0.75rem;
  font-size: 0.8rem;
}

/* 美化下拉框样式 */
.custom-select {
  position: relative;
  min-width: 200px;
  cursor: pointer;
  user-select: none;
}

.select-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1.5rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background-color: white;
  color: #333;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.custom-select:hover .select-display {
  border-color: #f6d55c;
  box-shadow: 0 4px 16px rgba(246, 213, 92, 0.15);
  transform: translateY(-2px);
}

.select-text {
  flex: 1;
}

.select-arrow {
  margin-left: 0.5rem;
  transition: transform 0.3s ease;
  color: #666;
  font-size: 0.8rem;
}

.select-arrow.rotated {
  transform: rotate(180deg);
}

.select-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background-color: white;
  border: 2px solid #f6d55c;
  border-top: none;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  margin-top: -2px;
  overflow: hidden;
  max-height: 200px;
  overflow-y: auto;
}

.select-option {
  padding: 1rem 1.5rem;
  font-size: 0.9rem;
  font-weight: 500;
  color: #333;
  background-color: white;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.select-option:last-child {
  border-bottom: none;
}

.select-option:hover {
  background-color: #fff8e1;
  color: #333;
}

.select-option.selected {
  background-color: #f6d55c;
  color: #333;
  font-weight: 600;
}

.select-option.selected:hover {
  background-color: #f3a712;
}

.publish-confirmation {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.confirmation-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.confirmation-list li {
  padding: 0.75rem 0;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.confirmation-list li:last-child {
  border-bottom: none;
}

.confirmation-list li:hover {
  background: #f8f9fa;
  padding-left: 0.5rem;
  border-radius: 8px;
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

/* 摘要输入框样式优化 */
.publish-page .excerpt-textarea,
.publish-page textarea.excerpt-textarea,
.publish-page .form-control.excerpt-textarea {
  min-height: 100px !important;
  max-height: 150px !important;
  resize: vertical !important;
  padding: 0.75rem !important;
  line-height: 1.5 !important;
  font-size: 0.95rem !important;
  height: auto !important;
  border-radius: 8px !important;
  border: 2px solid #dee2e6 !important;
  transition: all 0.3s ease !important;
}

.publish-page .excerpt-textarea:focus,
.publish-page textarea.excerpt-textarea:focus,
.publish-page .form-control.excerpt-textarea:focus {
  min-height: 100px !important;
  max-height: 180px !important;
  height: auto !important;
  border-color: #f6d55c !important;
  box-shadow: 0 0 0 0.2rem rgba(246, 213, 92, 0.25) !important;
}

/* 发布设置模态框样式优化 */
.publish-settings-modal {
  max-height: 85vh !important;
  overflow: hidden;
  z-index: 1055 !important;
  position: relative !important;
  border-radius: 16px !important;
}

.publish-settings-modal .modal-content {
  background: white !important;
  border: none !important;
  border-radius: 16px !important;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3) !important;
}

.publish-settings-modal .modal-header {
  padding: 1.5rem 2rem !important;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%) !important;
  border-bottom: 1px solid #e9ecef !important;
  border-radius: 16px 16px 0 0 !important;
}

.publish-settings-modal .modal-title {
  font-size: 1.4rem !important;
  font-weight: 700 !important;
  color: #2c3e50 !important;
  margin: 0 !important;
  line-height: 1.4 !important;
}

.publish-settings-modal .modal-title i {
  color: #f6d55c !important;
}

.publish-settings-modal .modal-body {
  max-height: calc(85vh - 180px) !important;
  overflow-y: auto !important;
  padding: 2rem !important;
}

.publish-settings-modal .modal-footer {
  padding: 1.5rem 2rem !important;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  border-top: 1px solid #e9ecef !important;
  border-radius: 0 0 16px 16px !important;
}

/* 设置卡片样式优化 */
.publish-settings-modal .setting-card {
  margin-bottom: 2rem !important;
}

.publish-settings-modal .setting-card:last-child {
  margin-bottom: 0 !important;
}

/* 摘要标题和按钮的布局 */
.setting-header-with-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.setting-header-with-buttons .setting-title {
  margin: 0;
  flex: 1;
  min-width: 200px;
}

.setting-header-with-buttons .ai-summary-buttons {
  display: flex;
  gap: 0.75rem;
  flex-shrink: 0;
}

.setting-header-with-buttons .ai-summary-buttons .btn {
  white-space: nowrap;
  font-size: 0.85rem;
  padding: 0.5rem 1rem;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .publish-header {
    margin-left: 1rem;
    margin-right: 1rem;
    padding: 1rem 1.5rem;
  }
  
  .publish-content {
    margin-left: 1rem;
    margin-right: 1rem;
  }
  
  .header-title-input {
    min-width: 400px;
  }
}

@media (max-width: 992px) {
  .publish-header {
    flex-direction: column;
    text-align: center;
    margin-left: 0.5rem;
    margin-right: 0.5rem;
    padding: 1rem;
  }
  
  .publish-content {
    margin-left: 0.5rem;
    margin-right: 0.5rem;
  }
  
  .edit-section {
    height: 100%;
    padding: 1rem;
  }
  
  .header-title-input {
    min-width: 100%;
    font-size: 1.3rem;
    text-align: center;
  }
  
  .title-section {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .publish-page {
    padding: 0.5rem 0;
  }
  
  .publish-header {
    padding: 1rem;
    margin: 0.5rem;
  }
  
  .header-title-input {
    font-size: 1.2rem;
  }
  
  .header-actions {
    justify-content: center;
    width: 100%;
  }
  
  .edit-section {
    padding: 0.75rem;
  }
  
  .editor-header-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .editor-toolbar {
    justify-content: center;
  }
  
  .editor-toolbar .btn {
    padding: 0.4rem 0.6rem;
    font-size: 0.8rem;
  }
  
  /* 移动端作品集输入优化 */
  .portfolio-input-group {
    padding: 1rem;
  }
  
  .portfolio-input-group .input-group {
    flex-direction: column;
  }
  
  .portfolio-input-group .input-group .form-control {
    border-radius: 8px;
    margin-bottom: 0.5rem;
  }
  
  .portfolio-input-group .input-group .btn {
    border-radius: 8px;
    border-left: 1px solid #dee2e6;
  }
  
  /* 移动端下拉框优化 */
  .custom-select {
    min-width: 100%;
  }
  
  .select-dropdown {
    max-height: 150px;
  }
  
  /* 移动端模态框优化 */
  .publish-settings-modal .modal-dialog {
    max-width: 98vw !important;
    margin: 2rem auto 1rem auto !important;
  }
  
  .publish-settings-modal .modal-body {
    max-height: calc(85vh - 120px) !important;
    padding: 1rem !important;
  }
  
  .publish-settings-modal .modal-header,
  .publish-settings-modal .modal-footer {
    padding: 1rem !important;
  }
  
  .publish-settings-modal .modal-title {
    font-size: 1.2rem !important;
  }
}

/* 确保模态框正确显示 */
.modal.show {
  display: block !important;
  background-color: rgba(0, 0, 0, 0.5) !important;
}

.modal-backdrop.show {
  opacity: 0.5 !important;
  z-index: 1049 !important;
}

/* 强制模态框显示 */
.publish-settings-modal {
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

/* 确保模态框内容可见 */
.publish-settings-modal .modal-content {
  background: white !important;
  border: 1px solid #dee2e6 !important;
  border-radius: 16px !important;
}

.publish-settings-modal .modal-dialog {
  margin-top: 4rem !important;
  margin-bottom: 2rem !important;
  position: relative !important;
  z-index: 1051 !important;
}

.publish-settings-modal .modal-content {
  position: relative !important;
  z-index: 1052 !important;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3) !important;
}

/* 响应式模态框优化 */
@media (max-width: 1200px) {
  .publish-settings-modal .modal-dialog {
    max-width: 95vw !important;
    margin: 3rem auto 1.5rem auto !important;
  }
  
  .publish-settings-modal .modal-body {
    max-height: calc(85vh - 140px) !important;
    padding: 1.5rem !important;
  }
}

/* 自定义滚动条样式 */
.wysiwyg-editor::-webkit-scrollbar,
.preview-content::-webkit-scrollbar {
  width: 8px;
}

.wysiwyg-editor::-webkit-scrollbar-track,
.preview-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.wysiwyg-editor::-webkit-scrollbar-thumb,
.preview-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.wysiwyg-editor::-webkit-scrollbar-thumb:hover,
.preview-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 确保预览内容区域在Firefox和其他浏览器中也能正确滚动 */
.preview-content {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}



/* 自定义滚动条样式 */
.content-editor::-webkit-scrollbar {
  width: 8px;
}

.content-editor::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.content-editor::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.content-editor::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 确保预览内容区域在Firefox和其他浏览器中也能正确滚动 */
.content-editor {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}
</style>
