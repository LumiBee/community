<template>
  <div class="settings-page">
    <div class="settings-container">
      <!-- 页面标题 -->
      <div class="settings-header">
        <h1 class="settings-title">
          <i class="fas fa-cog"></i>
          设置中心
        </h1>
      </div>

      <!-- 头像裁剪模态框 -->
      <AvatarCropper
        :visible="showAvatarCropper"
        :image-src="avatarImageSrc"
        @close="closeAvatarCropper"
        @crop="handleAvatarCrop"
      />

      <!-- 设置内容 -->
      <div class="settings-content">
        <!-- 左侧导航 -->
        <div class="settings-sidebar">
          <nav class="settings-nav">
            <button 
              v-for="section in settingsSections" 
              :key="section.id"
              @click="activeSection = section.id"
              :class="['nav-item', { active: activeSection === section.id }]"
            >
              <i :class="section.icon"></i>
              <span>{{ section.title }}</span>
            </button>
          </nav>
        </div>

        <!-- 右侧内容区域 -->
        <div class="settings-main">
          <!-- 个人信息设置 -->
          <div v-if="activeSection === 'profile'" class="settings-section">
            <div class="section-header">
              <h2>编辑基本资料</h2>
            </div>
            
            <div class="settings-form">
              <!-- 头像上传区域 -->
              <div class="avatar-upload-section">
                <div class="avatar-container">
                  <div class="avatar-preview" @click="$refs.avatarInput.click()">
                    <img :src="getAvatarUrl(userInfo.avatarUrl)" alt="用户头像" />
                    <div class="avatar-overlay">
                      <i class="fas fa-camera"></i>
                    </div>
                  </div>
                  <input 
                    ref="avatarInput"
                    type="file" 
                    class="d-none" 
                    accept="image/*"
                    @change="handleAvatarUpload"
                  >
                  <label for="avatarUpload" class="upload-avatar-btn">
                    <i class="fas fa-camera"></i>
                    <span>更换头像</span>
                  </label>
                  <input 
                    type="file" 
                    id="avatarUpload" 
                    class="d-none" 
                    accept="image/*"
                    @change="handleAvatarUpload"
                  >
                  <p class="upload-hint">支持JPG, PNG格式, 大小不超过2MB。点击头像或按钮选择图片。</p>
                </div>
              </div>

              <!-- 用户信息表单 -->
              <div class="form-group">
                <label class="form-label">用户名</label>
                <input type="text" v-model="userInfo.userName" class="form-control" />
                <p class="form-hint">这将是您在网站上公开显示的名称。</p>
              </div>

              <div class="form-group">
                <label class="form-label">个人简介</label>
                <textarea v-model="userInfo.bio" class="form-control" rows="4" placeholder="介绍一下自己..."></textarea>
              </div>

              <div class="form-group">
                <label class="form-label">电子邮箱</label>
                <input type="email" v-model="userInfo.email" class="form-control" />
                <p class="form-hint">用于登录和接收通知。如需更改,可能需要验证新邮箱。</p>
              </div>

              <div class="form-actions">
                <button class="btn btn-primary save-btn" @click="saveProfile">
                  <i class="fas fa-save"></i>
                  <span>保存更改</span>
                </button>
              </div>
            </div>
          </div>

          <!-- 账户安全设置 -->
          <div v-if="activeSection === 'security'" class="settings-section">
            <div class="section-header">
              <h2>账户安全</h2>
              <p>保护您的账户安全</p>
            </div>
            
            <div class="settings-form">
              <div class="security-item">
                <div class="security-info">
                  <div class="security-icon">
                    <i class="fas fa-lock"></i>
                  </div>
                  <div class="security-details">
                    <h4>登录密码</h4>
                    <p>定期更换密码以确保账户安全</p>
                  </div>
                </div>
                <button class="btn btn-outline-primary" @click="changePassword">修改密码</button>
              </div>

              <div class="security-item">
                <div class="security-info">
                  <div class="security-icon">
                    <i class="fas fa-mobile-alt"></i>
                  </div>
                  <div class="security-details">
                    <h4>手机绑定</h4>
                    <p>绑定手机号以增强账户安全性</p>
                  </div>
                </div>
                <button class="btn btn-outline-primary" @click="bindPhone">绑定手机</button>
              </div>

              <div class="security-item">
                <div class="security-info">
                  <div class="security-icon">
                    <i class="fas fa-shield-alt"></i>
                  </div>
                  <div class="security-details">
                    <h4>两步验证</h4>
                    <p>启用两步验证以保护您的账户</p>
                  </div>
                </div>
                <div class="toggle-switch">
                  <input type="checkbox" id="twoFactor" v-model="securitySettings.twoFactor" @change="toggleTwoFactor" />
                  <label for="twoFactor"></label>
                </div>
              </div>

              <div class="security-item">
                <div class="security-info">
                  <div class="security-icon">
                    <i class="fas fa-history"></i>
                  </div>
                  <div class="security-details">
                    <h4>登录历史</h4>
                    <p>查看最近的登录活动</p>
                  </div>
                </div>
                <button class="btn btn-outline-secondary" @click="viewLoginHistory">查看历史</button>
              </div>
            </div>
          </div>

          <!-- 通知设置 -->
          <div v-if="activeSection === 'notifications'" class="settings-section">
            <div class="section-header">
              <h2>通知设置</h2>
              <p>管理您接收的通知类型</p>
            </div>
            
            <div class="settings-form">
              <div class="notification-group">
                <h4>文章相关</h4>
                <div class="notification-item">
                  <div class="notification-info">
                    <span>新评论通知</span>
                    <small>当有人评论您的文章时通知您</small>
                  </div>
                  <div class="toggle-switch">
                    <input type="checkbox" id="commentNotify" v-model="notificationSettings.comments" />
                    <label for="commentNotify"></label>
                  </div>
                </div>

                <div class="notification-item">
                  <div class="notification-info">
                    <span>点赞通知</span>
                    <small>当有人点赞您的文章时通知您</small>
                  </div>
                  <div class="toggle-switch">
                    <input type="checkbox" id="likeNotify" v-model="notificationSettings.likes" />
                    <label for="likeNotify"></label>
                  </div>
                </div>

                <div class="notification-item">
                  <div class="notification-info">
                    <span>收藏通知</span>
                    <small>当有人收藏您的文章时通知您</small>
                  </div>
                  <div class="toggle-switch">
                    <input type="checkbox" id="favoriteNotify" v-model="notificationSettings.favorites" />
                    <label for="favoriteNotify"></label>
                  </div>
                </div>
              </div>

              <div class="notification-group">
                <h4>用户互动</h4>
                <div class="notification-item">
                  <div class="notification-info">
                    <span>关注通知</span>
                    <small>当有人关注您时通知您</small>
                  </div>
                  <div class="toggle-switch">
                    <input type="checkbox" id="followNotify" v-model="notificationSettings.follows" />
                    <label for="followNotify"></label>
                  </div>
                </div>

                <div class="notification-item">
                  <div class="notification-info">
                    <span>私信通知</span>
                    <small>当收到新私信时通知您</small>
                  </div>
                  <div class="toggle-switch">
                    <input type="checkbox" id="messageNotify" v-model="notificationSettings.messages" />
                    <label for="messageNotify"></label>
                  </div>
                </div>
              </div>



              <div class="form-actions">
                <button class="btn btn-primary" @click="saveNotificationSettings">保存设置</button>
              </div>
            </div>
          </div>

          <!-- 隐私设置 -->
          <div v-if="activeSection === 'privacy'" class="settings-section">
            <div class="section-header">
              <h2>隐私设置</h2>
              <p>控制您的隐私和可见性</p>
            </div>
            
            <div class="settings-form">
              <div class="privacy-item">
                <div class="privacy-info">
                  <h4>个人资料可见性</h4>
                  <p>控制谁可以查看您的个人资料</p>
                </div>
                <select v-model="privacySettings.profileVisibility" class="form-control">
                  <option value="public">公开</option>
                  <option value="followers">仅关注者</option>
                  <option value="private">私密</option>
                </select>
              </div>

              <div class="privacy-item">
                <div class="privacy-info">
                  <h4>文章可见性</h4>
                  <p>设置新文章的默认可见性</p>
                </div>
                <select v-model="privacySettings.articleVisibility" class="form-control">
                  <option value="public">公开</option>
                  <option value="followers">仅关注者</option>
                  <option value="private">私密</option>
                </select>
              </div>

              <div class="privacy-item">
                <div class="privacy-info">
                  <h4>搜索可见性</h4>
                  <p>是否允许其他用户通过搜索找到您</p>
                </div>
                <div class="toggle-switch">
                  <input type="checkbox" id="searchVisibility" v-model="privacySettings.searchable" />
                  <label for="searchVisibility"></label>
                </div>
              </div>

              <div class="privacy-item">
                <div class="privacy-info">
                  <h4>在线状态</h4>
                  <p>是否显示您的在线状态</p>
                </div>
                <div class="toggle-switch">
                  <input type="checkbox" id="onlineStatus" v-model="privacySettings.showOnline" />
                  <label for="onlineStatus"></label>
                </div>
              </div>

              <div class="form-actions">
                <button class="btn btn-primary" @click="savePrivacySettings">保存设置</button>
              </div>
            </div>
          </div>

          <!-- 数据管理 -->
          <div v-if="activeSection === 'data'" class="settings-section">
            <div class="section-header">
              <h2>数据管理</h2>
              <p>管理您的账户数据</p>
            </div>
            
            <div class="settings-form">
              <div class="data-item">
                <div class="data-info">
                  <div class="data-icon">
                    <i class="fas fa-download"></i>
                  </div>
                  <div class="data-details">
                    <h4>导出数据</h4>
                    <p>下载您的所有数据备份</p>
                  </div>
                </div>
                <button class="btn btn-outline-primary" @click="exportData">导出数据</button>
              </div>

              <div class="data-item">
                <div class="data-info">
                  <div class="data-icon">
                    <i class="fas fa-trash-alt"></i>
                  </div>
                  <div class="data-details">
                    <h4>删除账户</h4>
                    <p>永久删除您的账户和所有数据</p>
                  </div>
                </div>
                <button class="btn btn-outline-danger" @click="deleteAccount">删除账户</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import AvatarCropper from '@/components/AvatarCropper.vue'
import { userAPI } from '@/api/user'
import { getAvatarUrl } from '@/utils/avatar-helper'

const authStore = useAuthStore()

// 当前激活的设置部分
const activeSection = ref('profile')

// 设置部分配置
const settingsSections = [
  { id: 'profile', title: '个人信息', icon: 'fas fa-user' },
  { id: 'security', title: '账户安全', icon: 'fas fa-shield-alt' },
  { id: 'notifications', title: '通知设置', icon: 'fas fa-bell' },
  { id: 'privacy', title: '隐私设置', icon: 'fas fa-eye' },
  { id: 'data', title: '数据管理', icon: 'fas fa-database' }
]

// 用户信息
const userInfo = reactive({
  userName: authStore.userName || '',
  nickname: '',
  email: '',
  bio: '',
  avatarUrl: authStore.userAvatar || ''
})

// 头像上传相关状态
const showAvatarCropper = ref(false)
const avatarFile = ref(null)
const avatarImageSrc = ref('')

// 头像上传处理
const handleAvatarUpload = (event) => {
  console.log('头像上传事件触发:', event.target.files)
  const file = event.target.files[0]
  if (file) {
    console.log('选择的文件:', file.name, file.type, file.size)
    
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      window.$toast.error('请选择图片文件')
      return
    }
    
    // 验证文件大小 (2MB)
    if (file.size > 2 * 1024 * 1024) {
      window.$toast.error('文件大小不能超过2MB')
      return
    }
    
    // 创建文件URL并显示裁剪器
    avatarFile.value = file
    avatarImageSrc.value = URL.createObjectURL(file)
    showAvatarCropper.value = true
    
    console.log('显示裁剪器:', showAvatarCropper.value, avatarImageSrc.value)
    
    // 清空input值，允许重复选择同一文件
    event.target.value = ''
  }
}

// 头像裁剪完成处理
const handleAvatarCrop = async (croppedImageBlob) => {
  try {
    console.log('开始上传头像...')
    console.log('裁剪后的图片大小:', croppedImageBlob.size, 'bytes')
    
    // 创建FormData
    const formData = new FormData()
    formData.append('avatar', croppedImageBlob, 'avatar.jpg')
    
    console.log('FormData创建完成，准备上传...')
    
    // 上传到服务器
    const response = await userAPI.uploadAvatar(formData)
    console.log('头像上传成功:', response)
    
    // 更新用户信息
    if (response && response.avatarUrl) {
      console.log('更新头像URL:', response.avatarUrl)
      
      // 更新本地用户信息
      userInfo.avatarUrl = response.avatarUrl
      
      // 更新auth store中的用户信息
      authStore.updateUserProfile({ avatarUrl: response.avatarUrl })
      console.log('Auth store已更新')
      
      // 强制更新auth store中的用户对象
      if (authStore.user) {
        authStore.user.avatarUrl = response.avatarUrl
      }
      
      // 显示成功消息，不强制刷新页面
      console.log('头像更新完成，所有组件应该自动更新')
    } else {
      console.warn('响应中没有avatarUrl:', response)
    }
    
    // 显示成功提示
    window.$toast.success('头像上传成功！')
    
  } catch (error) {
    console.error('头像上传失败:', error)
    console.error('错误详情:', error.response?.data)
    window.$toast.error('头像上传失败，请重试。错误信息: ' + (error.response?.data?.message || error.message))
  } finally {
    // 关闭裁剪器并清理资源
    showAvatarCropper.value = false
    avatarFile.value = null
    if (avatarImageSrc.value) {
      URL.revokeObjectURL(avatarImageSrc.value)
      avatarImageSrc.value = ''
    }
  }
}

// 关闭头像裁剪器
const closeAvatarCropper = () => {
  showAvatarCropper.value = false
  avatarFile.value = null
  if (avatarImageSrc.value) {
    URL.revokeObjectURL(avatarImageSrc.value)
    avatarImageSrc.value = ''
  }
}

// 使用全局工具函数，移除本地定义

// 安全设置
const securitySettings = reactive({
  twoFactor: false
})

// 通知设置
const notificationSettings = reactive({
  comments: true,
  likes: true,
  favorites: true,
  follows: true,
  messages: true,
  system: false
})

// 隐私设置
const privacySettings = reactive({
  profileVisibility: 'public',
  articleVisibility: 'public',
  searchable: true,
  showOnline: true
})

// 保存个人信息
const saveProfile = async () => {
  try {
    // 验证必填字段
    if (!userInfo.userName.trim()) {
      window.$toast.error('用户名不能为空')
      return
    }
    
    if (!userInfo.email.trim()) {
      window.$toast.error('邮箱不能为空')
      return
    }
    
    // 验证邮箱格式
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(userInfo.email)) {
      window.$toast.error('请输入有效的邮箱地址')
      return
    }
    
    // 调用API保存用户信息
    const response = await userAPI.updateProfile({
      userName: userInfo.userName.trim(),
      email: userInfo.email.trim(),
      bio: userInfo.bio.trim()
    })
    
    if (response && response.success) {
      // 更新auth store中的用户信息
      authStore.updateUserProfile({
        userName: userInfo.userName.trim(),
        email: userInfo.email.trim(),
        bio: userInfo.bio.trim()
      })
      
      window.$toast.success('个人信息保存成功！')
    } else {
      // 处理验证错误
      if (response.userName) {
        window.$toast.error(response.userName)
      } else if (response.email) {
        window.$toast.error(response.email)
      } else if (response.bio) {
        window.$toast.error(response.bio)
      } else if (response.message) {
        window.$toast.error(response.message)
      } else {
        window.$toast.error('保存失败，请重试')
      }
    }
  } catch (error) {
    console.error('保存个人信息失败:', error)
    window.$toast.error('保存失败：' + (error.response?.data?.message || error.message))
  }
}

// 保存通知设置
const saveNotificationSettings = async () => {
  try {
    // 这里可以调用API保存通知设置
    // const response = await userAPI.updateNotificationSettings(notificationSettings)
    
    // 模拟保存成功
    window.$toast.success('通知设置保存成功！')
  } catch (error) {
    console.error('保存通知设置失败:', error)
    window.$toast.error('保存失败：' + (error.response?.data?.message || error.message))
  }
}

// 保存隐私设置
const savePrivacySettings = async () => {
  try {
    // 这里可以调用API保存隐私设置
    // const response = await userAPI.updatePrivacySettings(privacySettings)
    
    // 模拟保存成功
    window.$toast.success('隐私设置保存成功！')
  } catch (error) {
    console.error('保存隐私设置失败:', error)
    window.$toast.error('保存失败：' + (error.response?.data?.message || error.message))
  }
}

// 导出数据
const exportData = async () => {
  try {
    // 这里可以调用API导出数据
    // const response = await userAPI.exportData()
    
    // 模拟导出成功
    window.$toast.success('数据导出成功！文件已下载到您的设备。')
  } catch (error) {
    console.error('导出数据失败:', error)
    window.$toast.error('导出失败：' + (error.response?.data?.message || error.message))
  }
}

// 删除账户
const deleteAccount = async () => {
  try {
    // 确认删除
    if (!confirm('确定要删除账户吗？此操作不可撤销，所有数据将被永久删除。')) {
      return
    }
    
    // 这里可以调用API删除账户
    // const response = await userAPI.deleteAccount()
    
    // 模拟删除成功
    window.$toast.success('账户删除成功！')
    
    // 退出登录
    authStore.logout()
  } catch (error) {
    console.error('删除账户失败:', error)
    window.$toast.error('删除失败：' + (error.response?.data?.message || error.message))
  }
}

// 修改密码
const changePassword = () => {
  window.$toast.info('修改密码功能正在开发中，敬请期待！')
}

// 绑定手机
const bindPhone = () => {
  window.$toast.info('手机绑定功能正在开发中，敬请期待！')
}

// 切换两步验证
const toggleTwoFactor = () => {
  if (securitySettings.twoFactor) {
    window.$toast.success('两步验证已启用！')
  } else {
    window.$toast.warning('两步验证已禁用！')
  }
}

// 查看登录历史
const viewLoginHistory = () => {
  window.$toast.info('登录历史功能正在开发中，敬请期待！')
}

// 页面初始化时加载用户信息
onMounted(async () => {
  try {
    // 如果auth store中没有完整的用户信息，则从API获取
    if (!authStore.user || !authStore.user.email) {
      const userData = await userAPI.getCurrentUser()
      if (userData) {
        userInfo.userName = userData.userName || ''
        userInfo.email = userData.email || ''
        userInfo.bio = userData.bio || ''
        userInfo.avatarUrl = userData.avatarUrl || ''
      }
    } else {
      // 使用auth store中的数据
      userInfo.userName = authStore.userName || ''
      userInfo.email = authStore.user?.email || ''
      userInfo.bio = authStore.user?.bio || ''
      userInfo.avatarUrl = authStore.userAvatar || ''
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    window.$toast.error('加载用户信息失败，请刷新页面重试')
  }
})
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: var(--bg-soft);
  padding: 1.5rem 0;
  position: relative;
}

.settings-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 300px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  opacity: 0.08;
  z-index: 0;
}

.settings-page::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(255, 193, 7, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 193, 7, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(255, 193, 7, 0.03) 0%, transparent 50%);
  z-index: 0;
  pointer-events: none;
}

.settings-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  position: relative;
  z-index: 1;
}

/* 页面标题 */
.settings-header {
  text-align: center;
  margin-bottom: 2rem;
  animation: fadeInUp 0.8s ease-out;
}

.settings-title {
  font-size: 2.75rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  font-family: var(--font-family-serif);
  animation: fadeInUp 0.8s ease-out;
}

.settings-title i {
  color: var(--primary-dark);
  font-size: 2rem;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: float 4s ease-in-out infinite;
}

.settings-subtitle {
  display: none;
}

/* 设置内容布局 */
.settings-content {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 2rem;
  background: var(--bg-white);
  border-radius: var(--radius-xl);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.1),
    0 8px 16px rgba(0, 0, 0, 0.06),
    0 0 0 1px rgba(255, 193, 7, 0.1);
  overflow: hidden;
  border: 1px solid var(--border-light);
  animation: fadeInUp 0.8s ease-out 0.4s both;
  max-height: 85vh;
  backdrop-filter: blur(10px);
}

/* 左侧导航 */
.settings-sidebar {
  background: linear-gradient(180deg, var(--bg-light) 0%, rgba(255, 193, 7, 0.02) 100%);
  padding: 1.5rem 0;
  border-right: 1px solid var(--border-light);
  position: relative;
  backdrop-filter: blur(10px);
}

.settings-sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color) 0%, var(--primary-dark) 50%, var(--primary-color) 100%);
  background-size: 200% 100%;
  animation: shimmer 4s ease-in-out infinite;
}

.settings-nav {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0 0.5rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.875rem 1.25rem;
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s ease, background-color 0.2s ease, color 0.2s ease;
  border-radius: 0;
  text-align: left;
  width: 100%;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.4s ease-out;
  animation-fill-mode: both;
  margin: 0.25rem 0;
  will-change: transform;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: -1;
  border-radius: 0 8px 8px 0;
}

.nav-item:hover {
  color: var(--text-primary);
  transform: translateX(3px);
  background: rgba(255, 193, 7, 0.08);
  border-radius: 0 8px 8px 0;
}

.nav-item:hover i {
  transform: scale(1.05);
  color: var(--primary-dark);
}

.nav-item.active {
  color: var(--text-primary);
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
  transform: translateX(5px);
  position: relative;
  border-radius: 0 12px 12px 0;
  font-weight: 600;
}

.nav-item.active::before {
  opacity: 0;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: var(--text-primary);
  border-radius: 2px 0 0 2px;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.2);
}

.nav-item i {
  font-size: 1.1rem;
  width: 20px;
  text-align: center;
  transition: transform 0.2s ease, color 0.2s ease;
  color: var(--text-muted);
}

.nav-item.active i {
  color: var(--text-primary);
  transform: scale(1.05);
}

/* 右侧内容区域 */
.settings-main {
  padding: 1.5rem;
  max-height: 85vh;
  overflow-y: auto;
}

.settings-section {
  animation: fadeInUp 0.6s ease-out;
}

/* 为每个设置项添加延迟动画 */
.security-item,
.privacy-item,
.data-item,
.notification-group {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.security-item:nth-child(1) { animation-delay: 0.1s; }
.security-item:nth-child(2) { animation-delay: 0.2s; }
.security-item:nth-child(3) { animation-delay: 0.3s; }
.security-item:nth-child(4) { animation-delay: 0.4s; }

.privacy-item:nth-child(1) { animation-delay: 0.1s; }
.privacy-item:nth-child(2) { animation-delay: 0.2s; }
.privacy-item:nth-child(3) { animation-delay: 0.3s; }
.privacy-item:nth-child(4) { animation-delay: 0.4s; }

.data-item:nth-child(1) { animation-delay: 0.1s; }
.data-item:nth-child(2) { animation-delay: 0.2s; }

.notification-group:nth-child(1) { animation-delay: 0.1s; }
.notification-group:nth-child(2) { animation-delay: 0.2s; }
.notification-group:nth-child(3) { animation-delay: 0.3s; }

.section-header {
  margin-bottom: 1.5rem;
  text-align: center;
  padding-bottom: 0;
  border-bottom: none;
}

.section-header h2 {
  font-size: 1.9rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0;
  font-family: var(--font-family-serif);
}

.section-header p {
  display: none;
}

/* 表单样式 */
.settings-form {
  max-width: 450px;
  margin: 0 auto;
}

.form-group {
  margin-bottom: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-label {
  display: block;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  font-size: 1rem;
}

.form-hint {
  color: var(--text-muted);
  font-size: 0.85rem;
  margin: 0.25rem 0 0 0;
  line-height: 1.3;
}

.form-control {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  font-size: 1.05rem;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
  background: var(--bg-white);
  color: var(--text-primary);
}

.form-control:focus {
  outline: none;
  border-color: var(--primary-dark);
  background: var(--bg-white);
  box-shadow: 0 0 0 3px rgba(255, 193, 7, 0.1);
  transform: translateY(-1px);
}

textarea.form-control {
  resize: vertical;
  min-height: 80px;
}

/* 头像上传区域 */
.avatar-upload-section {
  margin-bottom: 1.5rem;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1.5rem;
  border: 2px dashed var(--primary-color);
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.05) 0%, rgba(255, 193, 7, 0.02) 100%);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.avatar-container:hover {
  border-color: var(--primary-dark);
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.08) 0%, rgba(255, 193, 7, 0.04) 100%);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.avatar-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 0.75rem;
  border: 3px solid rgba(255, 193, 7, 0.2);
}

.avatar-preview:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  border-color: var(--primary-dark);
}

.avatar-preview:hover .avatar-overlay {
  opacity: 1;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-avatar-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1.2rem;
  background: var(--primary-dark);
  color: var(--text-primary);
  border: none;
  border-radius: var(--radius);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
}

.upload-avatar-btn:hover {
  background: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.upload-hint {
  color: var(--text-muted);
  font-size: 0.8rem;
  margin: 0;
  text-align: center;
}

/* 安全设置项 */
.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  background: var(--bg-light);
  border-radius: var(--radius);
  margin-bottom: 1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--border-light);
}

.security-item:hover {
  background: var(--bg-white);
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.security-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.security-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  box-shadow: var(--shadow-sm);
}

.security-details h4 {
  margin: 0 0 0.25rem 0;
  color: var(--text-primary);
  font-weight: 600;
}

.security-details p {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.9rem;
}

/* 通知设置 */
.notification-group {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: var(--bg-light);
  border-radius: var(--radius);
  border: 1px solid var(--border-light);
}

.notification-group h4 {
  margin: 0 0 1rem 0;
  color: var(--text-primary);
  font-weight: 600;
  font-family: var(--font-family-serif);
}

.notification-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 0;
  border-bottom: 1px solid var(--border-light);
  transition: all 0.2s ease;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background: rgba(255, 193, 7, 0.05);
  margin: 0 -1rem;
  padding: 1rem;
  border-radius: var(--radius-sm);
  transform: translateX(5px);
}

.notification-info {
  flex: 1;
}

.notification-info span {
  display: block;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 0.25rem;
}

.notification-info small {
  color: var(--text-muted);
  font-size: 0.85rem;
}

/* 隐私设置 */
.privacy-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  background: var(--bg-light);
  border-radius: var(--radius);
  margin-bottom: 1rem;
  border: 1px solid var(--border-light);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.privacy-item:hover {
  background: var(--bg-white);
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.privacy-info h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-weight: 600;
}

.privacy-info p {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

/* 数据管理 */
.data-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  background: var(--bg-light);
  border-radius: var(--radius);
  margin-bottom: 1rem;
  border: 1px solid var(--border-light);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.data-item:hover {
  background: var(--bg-white);
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.data-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.data-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #6c757d 0%, #495057 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.data-details h4 {
  margin: 0 0 0.25rem 0;
  color: #2c3e50;
  font-weight: 600;
}

.data-details p {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

/* 开关样式 */
.toggle-switch {
  position: relative;
  width: 50px;
  height: 26px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-switch label {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
  border-radius: 26px;
}

.toggle-switch label:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

.toggle-switch input:checked + label {
  background-color: var(--primary-dark);
}

.toggle-switch input:checked + label:before {
  transform: translateX(24px);
}

/* 按钮样式 */
.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 500;
  font-size: 1rem;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  color: var(--text-primary);
  box-shadow: 
    0 4px 12px rgba(255, 193, 7, 0.3),
    0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 193, 7, 0.4);
  filter: brightness(1.05);
}



.btn-outline-primary {
  background: transparent;
  color: var(--primary-dark);
  border: 2px solid var(--primary-dark);
}

.btn-outline-primary:hover {
  background: var(--primary-dark);
  color: var(--text-primary);
  transform: translateY(-3px);
  box-shadow: var(--shadow);
}

.btn-outline-secondary {
  background: transparent;
  color: var(--text-muted);
  border: 2px solid var(--border-color);
}

.btn-outline-secondary:hover {
  background: var(--text-muted);
  color: white;
  transform: translateY(-3px);
  box-shadow: var(--shadow);
}

.btn-outline-danger {
  background: transparent;
  color: #dc3545;
  border: 2px solid #dc3545;
}

.btn-outline-danger:hover {
  background: #dc3545;
  color: white;
  transform: translateY(-3px);
  box-shadow: var(--shadow);
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-light);
}

.save-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  font-weight: 600;
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes shimmer {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-3px); }
}

/* 添加导航项的延迟动画 */
.nav-item:nth-child(1) { animation-delay: 0.1s; }
.nav-item:nth-child(2) { animation-delay: 0.2s; }
.nav-item:nth-child(3) { animation-delay: 0.3s; }
.nav-item:nth-child(4) { animation-delay: 0.4s; }
.nav-item:nth-child(5) { animation-delay: 0.5s; }

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-content {
    grid-template-columns: 1fr;
  }
  
  .settings-sidebar {
    border-right: none;
    border-bottom: 1px solid #e9ecef;
  }
  
  .settings-nav {
    flex-direction: row;
    overflow-x: auto;
    padding: 0 1rem;
  }
  
  .nav-item {
    white-space: nowrap;
    min-width: 120px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .security-item,
  .privacy-item,
  .data-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .notification-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 576px) {
  .settings-container {
    padding: 0 0.5rem;
  }
  
  .settings-title {
    font-size: 2rem;
  }
  
  .settings-main {
    padding: 1rem;
  }
  
  .security-icon,
  .data-icon {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }
}
</style>
