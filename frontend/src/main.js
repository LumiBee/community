import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 导入样式
import 'bootstrap/dist/css/bootstrap.min.css'
import '@fortawesome/fontawesome-free/css/all.min.css'
import 'aos/dist/aos.css'
import './assets/styles/main.scss'
import './assets/styles/components.scss'

// 导入JavaScript库
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import AOS from 'aos'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 初始化AOS动画库
AOS.init({
  duration: 800,
  once: true,
  offset: 100
})

app.mount('#app')
