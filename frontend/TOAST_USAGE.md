# Toast 提示框使用指南

## 概述

本项目集成了一个现代化的 Toast 提示框组件，提供了四种类型的提示：成功、错误、警告和信息。Toast 提示框会自动显示在页面右上角，并在指定时间后自动消失。

## 功能特性

- ✅ **四种提示类型**：成功、错误、警告、信息
- ✅ **自动消失**：可自定义显示时长
- ✅ **点击关闭**：点击 Toast 可手动关闭
- ✅ **动画效果**：平滑的进入和退出动画
- ✅ **响应式设计**：在移动设备上自适应显示
- ✅ **全局调用**：可在任何组件中使用

## 使用方法

### 1. 在 Vue 组件中使用

#### Composition API 方式

```javascript
import { getCurrentInstance } from 'vue'

export default {
  setup() {
    const { proxy } = getCurrentInstance()
    
    const showSuccess = () => {
      proxy.$toast.success('操作成功！')
    }
    
    const showError = () => {
      proxy.$toast.error('操作失败，请重试！')
    }
    
    const showWarning = () => {
      proxy.$toast.warning('请注意，这是一个警告！')
    }
    
    const showInfo = () => {
      proxy.$toast.info('这是一条信息提示！')
    }
    
    return {
      showSuccess,
      showError,
      showWarning,
      showInfo
    }
  }
}
```

#### Options API 方式

```javascript
export default {
  methods: {
    showSuccess() {
      this.$toast.success('操作成功！')
    },
    
    showError() {
      this.$toast.error('操作失败，请重试！')
    },
    
    showWarning() {
      this.$toast.warning('请注意，这是一个警告！')
    },
    
    showInfo() {
      this.$toast.info('这是一条信息提示！')
    }
  }
}
```

### 2. 全局调用（非 Vue 组件）

```javascript
// 在任何 JavaScript 代码中
window.$toast.success('操作成功！')
window.$toast.error('操作失败！')
window.$toast.warning('警告信息！')
window.$toast.info('提示信息！')
```

### 3. 自定义显示时长

```javascript
// 显示 5 秒
proxy.$toast.success('操作成功！', 5000)

// 显示 1 秒
proxy.$toast.error('操作失败！', 1000)
```

## API 参考

### 方法

| 方法 | 参数 | 说明 |
|------|------|------|
| `success(message, duration)` | `message`: 消息内容<br>`duration`: 显示时长（毫秒，默认3000） | 显示成功提示 |
| `error(message, duration)` | `message`: 消息内容<br>`duration`: 显示时长（毫秒，默认3000） | 显示错误提示 |
| `warning(message, duration)` | `message`: 消息内容<br>`duration`: 显示时长（毫秒，默认3000） | 显示警告提示 |
| `info(message, duration)` | `message`: 消息内容<br>`duration`: 显示时长（毫秒，默认3000） | 显示信息提示 |

### 样式定制

Toast 组件使用以下颜色主题：

- **成功**：黄色边框和图标 (#ffda58)
- **错误**：红色边框和图标 (#dc3545)
- **警告**：橙色边框和图标 (#ffc107)
- **信息**：蓝色边框和图标 (#17a2b8)

## 实际应用示例

### 1. 表单提交

```javascript
const submitForm = async () => {
  try {
    await api.submitForm(formData)
    proxy.$toast.success('表单提交成功！')
  } catch (error) {
    proxy.$toast.error('表单提交失败，请重试！')
  }
}
```

### 2. 文件上传

```javascript
const uploadFile = async (file) => {
  try {
    await api.uploadFile(file)
    proxy.$toast.success('文件上传成功！')
  } catch (error) {
    proxy.$toast.error('文件上传失败：' + error.message)
  }
}
```

### 3. 数据删除

```javascript
const deleteItem = async (id) => {
  try {
    await api.deleteItem(id)
    proxy.$toast.success('删除成功！')
    loadData() // 重新加载数据
  } catch (error) {
    proxy.$toast.error('删除失败，请重试！')
  }
}
```

### 4. 网络错误处理

```javascript
const fetchData = async () => {
  try {
    const data = await api.getData()
    // 处理数据
  } catch (error) {
    if (error.code === 401) {
      proxy.$toast.warning('登录已过期，请重新登录')
      // 跳转到登录页
    } else if (error.code === 403) {
      proxy.$toast.error('没有权限执行此操作')
    } else {
      proxy.$toast.error('网络错误，请检查网络连接')
    }
  }
}
```

## 测试页面

访问 `/toast-test` 路由可以查看 Toast 功能的演示页面，包含各种类型的提示示例和自定义选项。

## 注意事项

1. **自动消失**：Toast 会在指定时间后自动消失，用户也可以点击手动关闭
2. **多个 Toast**：可以同时显示多个 Toast，它们会垂直堆叠
3. **响应式**：在移动设备上，Toast 会占据更多宽度以确保可读性
4. **Z-index**：Toast 的 z-index 为 1090，确保在其他元素之上显示

## 技术实现

- **Vue 3 Composition API**
- **Teleport** 用于将 Toast 渲染到 body 元素
- **TransitionGroup** 用于动画效果
- **插件系统** 用于全局注册
- **响应式设计** 适配各种屏幕尺寸
