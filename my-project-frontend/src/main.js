import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from "axios";
import { createPinia } from "pinia";

import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/assets/quill.css'

axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
