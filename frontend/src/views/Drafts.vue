<template>
  <div class="drafts-page">
    <div class="container-fluid" style="max-width: 1400px;">
      <!-- 页面标题 -->
      <h5 class="font-weight-bold spanborder">
        <span>我的草稿</span>
      </h5>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-4">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
        <p class="mt-2">正在加载草稿...</p>
      </div>
      
      <!-- 空状态提示 -->
      <div v-else-if="drafts.length === 0" class="text-center py-5">
        <i class="fas fa-file-alt fa-3x text-muted mb-3"></i>
        <h5 class="text-muted">暂无草稿</h5>
        <p class="text-muted">您还没有保存任何草稿</p>
        <router-link to="/publish" class="btn btn-primary">
          <i class="fas fa-plus"></i> 创建新文章
        </router-link>
      </div>
      
      <!-- 草稿列表 -->
      <ol class="list-unstyled compact-article-list" v-if="drafts.length > 0">
        <li
          v-for="(draft, index) in drafts"
          :key="draft.articleId"
          class="compact-article-item"
          data-aos="fade-up"
          :data-aos-delay="index * 50"
        >
          <div class="compact-article-content">
            <div class="draft-header">
              <h6 class="compact-article-title">
                <span class="text-dark">
                  {{ draft.title || '无标题草稿' }}
                </span>
              </h6>
              <div class="draft-actions">
                <button class="btn btn-sm btn-outline-primary me-2" @click="editDraft(draft.articleId)">
                  <i class="fas fa-edit"></i> 编辑
                </button>
                <button class="btn btn-sm btn-outline-danger" @click="confirmDeleteDraft(draft.articleId)">
                  <i class="fas fa-trash-alt"></i> 删除
                </button>
              </div>
            </div>
            <p class="compact-article-excerpt">
              {{ draft.content ? (draft.content.length > 200 ? draft.content.substring(0, 200) + '...' : draft.content) : '暂无内容' }}
            </p>
            <div class="compact-article-meta">
              <div class="compact-author-info">
                <span class="draft-date">
                  <i class="far fa-clock"></i> 最后编辑于 {{ formatTime(draft.gmtModified || draft.gmtCreate) }}
                </span>
                <span class="draft-tag" v-if="draft.tags && draft.tags.length > 0">
                  <i class="fas fa-tags"></i> {{ draft.tags.join(', ') }}
                </span>
              </div>
              <!-- 移除了多余的按钮 -->
            </div>
          </div>
        </li>
      </ol>
      
      <!-- 空状态显示 -->
      <div v-else-if="!loading" class="empty-state">
        <div class="empty-state-icon">📝</div>
        <h3 class="empty-state-title">暂无草稿</h3>
        <p class="empty-state-text">您还没有保存任何草稿</p>
        <router-link to="/publish" class="btn btn-warning mt-3">
          <i class="fas fa-plus"></i> 创建新文章
        </router-link>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-warning" role="status">
          <span class="visually-hidden">加载中...</span>
        </div>
        <p class="mt-2 text-muted">正在加载草稿...</p>
      </div>
    </div>
    
    <!-- 删除草稿确认弹窗 -->
    <ConfirmDeleteModal
      :visible="showDeleteDraftModal"
      title="删除草稿"
      :message="`您确定要删除草稿「${draftToDelete?.title || '无标题草稿'}」吗？`"
      warning-message="删除后，该草稿将无法恢复。"
      confirm-text="确认删除"
      @confirm="deleteDraft"
      @cancel="closeDeleteDraftModal"
      @close="closeDeleteDraftModal"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleAPI } from '@/api'
import ConfirmDeleteModal from '@/components/ConfirmDeleteModal.vue'

const router = useRouter()
const drafts = ref([])
const loading = ref(true)
const draftToDelete = ref(null)
const showDeleteDraftModal = ref(false)

// 格式化时间
const formatTime = (dateString) => {
  if (!dateString) return '未知时间'
  
  const now = new Date()
  const date = new Date(dateString)
  const diff = now - date
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days > 0) return `${days} 天前`
  if (hours > 0) return `${hours} 小时前`
  if (minutes > 0) return `${minutes} 分钟前`
  return '刚刚'
}

// 加载草稿列表
const loadDrafts = async () => {
  try {
    loading.value = true
    const response = await articleAPI.getDrafts()
    console.log('草稿列表API响应:', response)
    
    // 检查响应数据结构
    if (response && response.records) {
      drafts.value = response.records
      console.log('草稿列表数据:', drafts.value)
      if (drafts.value.length > 0) {
        console.log('第一个草稿的数据结构:', drafts.value[0])
      }
    } else if (response && response.data && response.data.records) {
      drafts.value = response.data.records
      console.log('草稿列表数据:', drafts.value)
      if (drafts.value.length > 0) {
        console.log('第一个草稿的数据结构:', drafts.value[0])
      }
    } else {
      drafts.value = []
    }
  } catch (error) {
    console.error('加载草稿失败:', error)
    drafts.value = []
  } finally {
    loading.value = false
  }
}

// 编辑草稿
const editDraft = (draftId) => {
  console.log('点击编辑草稿，ID:', draftId)
  router.push(`/publish?draft=${draftId}`)
}

// 发布草稿
const publishDraft = async (draftId) => {
  try {
    // 先获取草稿详情
    const draft = await articleAPI.getDraftById(draftId)
    if (draft && draft.data) {
      // 跳转到发布页面并带上草稿数据
      router.push({
        path: '/publish',
        query: { draft: draftId, publish: true }
      })
    }
  } catch (error) {
    console.error('获取草稿详情失败:', error)
  }
}

// 确认删除草稿
const confirmDeleteDraft = (draftId) => {
  // 找到要删除的草稿对象
  const draft = drafts.value.find(d => d.articleId === draftId)
  draftToDelete.value = draft
  showDeleteDraftModal.value = true
}

// 删除草稿
const deleteDraft = async () => {
  if (!draftToDelete.value) return
  
  try {
    await articleAPI.deleteDraft(draftToDelete.value.articleId)
    
    // 重新加载草稿列表
    await loadDrafts()
    
    // 显示成功提示
    window.$toast.success(`草稿「${draftToDelete.value.title || '无标题草稿'}」已成功删除`)
  } catch (error) {
    console.error('删除草稿失败:', error)
    window.$toast.error('删除草稿失败，请稍后重试')
  } finally {
    closeDeleteDraftModal()
  }
}

// 关闭删除草稿弹窗
const closeDeleteDraftModal = () => {
  showDeleteDraftModal.value = false
  draftToDelete.value = null
}

onMounted(() => {
  loadDrafts()
})
</script>

<style scoped>
.drafts-page {
  padding: 2rem 0;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  min-height: 100vh;
}

/* 标题样式 */
.spanborder {
  position: relative;
  margin-bottom: 2.5rem;
  padding-bottom: 1.5rem;
}

.spanborder::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, #dee2e6 0%, transparent 100%);
}

.spanborder span {
  display: inline-block;
  background: white;
  padding-right: 2rem;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  position: relative;
}

.spanborder span::before {
  content: '';
  position: absolute;
  bottom: -1.5rem;
  left: 0;
  width: 3rem;
  height: 3px;
  background: linear-gradient(90deg, #ffc107 0%, #ffda58 100%);
  border-radius: 1.5px;
}

/* 草稿卡片样式 */
.compact-article-item {
  padding: 1.5rem;
  border: 1px solid #eaedf1;
  border-radius: 12px;
  transition: all 0.3s ease;
  margin-bottom: 1.5rem;
  position: relative;
  background-color: #ffffff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.compact-article-item:hover {
  background: linear-gradient(135deg, rgba(255, 255, 255, 1) 0%, rgba(255, 252, 240, 1) 100%);
  transform: translateY(-5px);
  border-color: rgba(255, 193, 7, 0.2);
  box-shadow: 0 10px 20px rgba(255, 193, 7, 0.1);
}

.draft-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.8rem;
}

.compact-article-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.draft-actions {
  display: flex;
  gap: 0.5rem;
}

.compact-article-excerpt {
  color: #64748b;
  font-size: 1.15rem;
  line-height: 1.6;
  margin: 0.8rem 0 1.2rem 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 5.5rem;
}

.compact-article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f1f5f9;
}

.compact-author-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.draft-date, .draft-tag {
  color: #64748b;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.draft-date i, .draft-tag i {
  color: #ffc107;
  opacity: 0.8;
}

/* 移除了不再使用的按钮样式 */

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 1.5rem;
  border: 1px solid rgba(255, 193, 7, 0.08);
  margin: 2rem 0;
  position: relative;
  overflow: hidden;
}

.empty-state::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #ffc107 0%, #ffda58 100%);
  border-radius: 1.5rem 1.5rem 0 0;
}

.empty-state-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
  opacity: 0.8;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

.empty-state-title {
  color: #2c3e50;
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  line-height: 1.4;
}

.empty-state-text {
  color: #64748b;
  font-size: 1.1rem;
  margin-bottom: 0;
  line-height: 1.6;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .draft-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .draft-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .compact-article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  /* 移除了不再使用的按钮样式 */
}
</style>
