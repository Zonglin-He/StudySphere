import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import axios from 'axios'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'

import '@/assets/quill.css'

axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

// ★ 兜底：若已有 token，开局就带上
const savedAuth = localStorage.getItem('authorization') || localStorage.getItem('token')
if (savedAuth) axios.defaults.headers.common.Authorization = savedAuth

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
