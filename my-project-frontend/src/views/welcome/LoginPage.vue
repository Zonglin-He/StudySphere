<template>
  <div style="text-align:center;margin:0 20px">
    <div style="margin-top: 40px">
      <div style="font-size: 24px; font-weight: 700">Log In</div>
      <div style="font-size: 14px; color: grey">Enter your username and password to sign in</div>
    </div>

    <div style="margin-top: 30px">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item prop="username" label="Username / Email">
          <el-input v-model="form.username" maxlength="50" />
        </el-form-item>

        <el-form-item prop="password" label="Password">
          <el-input v-model="form.password" type="password" maxlength="50" show-password />
        </el-form-item>

        <el-row style="margin-top: 5px">
          <el-col :span="12" style="text-align:left">
            <el-form-item prop="remember">
              <el-checkbox v-model="form.remember" label="Remember me"/>
            </el-form-item>
          </el-col>
          <el-col :span="12" style="text-align:right">
            <el-link @click="router.push('/forget')">Forgot password?</el-link>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <div style="margin-top: 18px">
      <el-button type="success" plain style="width: 270px" :loading="submitting" @click="userLogin">
        Log In Now
      </el-button>
    </div>

    <el-divider><span style="color:grey;font-size:13px">Don't have an account</span></el-divider>

    <div>
      <el-button type="warning" plain style="width: 270px" @click="router.push('/register')">
        Sign Up
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  username: '',
  password: '',
  remember: true,
})

const rules: FormRules = {
  username: [{ required: true, message: 'Please enter your username' }],
  password: [{ required: true, message: 'Please enter your password' }],
}

onMounted(() => {
  const remembered = localStorage.getItem('remember_username')
  if (remembered) form.username = remembered
})

function normalizeBearer(token?: string) {
  return token && token.startsWith('Bearer ') ? token : `Bearer ${token ?? ''}`
}

function parseExpire(expire: unknown): number {
  if (typeof expire === 'number') return expire
  if (typeof expire === 'string') {
    const t = new Date(expire.replace(' ', 'T')).getTime()
    if (!Number.isNaN(t)) return t
  }
  // 解析失败给一个兜底（7 天）
  return Date.now() + 7 * 24 * 3600 * 1000
}

async function userLogin() {
  const ok = await (formRef.value?.validate().then(() => true).catch(() => false))
  if (!ok) return
  submitting.value = true
  try {
    // Spring Security formLogin 需要 x-www-form-urlencoded
    const body = new URLSearchParams()
    body.append('username', form.username)
    body.append('password', form.password)

    // 如果在 main.js 里设置了 axios.defaults.baseURL = '/api'，这里路径写 '/auth/login'
    const res = await axios.post('/auth/login', body, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    })

    const payload = res.data?.data ?? res.data
    const token   = normalizeBearer(payload?.token)
    if (!token || token === 'Bearer ') throw new Error('No token in response')

    const auth = {
      token,
      expire: parseExpire(payload?.expire),
      role: payload?.role,
    }
    const str = JSON.stringify(auth)
    if (form.remember) localStorage.setItem('authorize', str)
    else sessionStorage.setItem('authorize', str)

    axios.defaults.headers.common.Authorization = auth.token
    if (form.remember) localStorage.setItem('remember_username', form.username)
    else localStorage.removeItem('remember_username')

    ElMessage.success(`Welcome ${payload?.username ?? form.username}!`)
    await router.replace('/index')    // 用 replace，避免回退回登录页
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || 'Login failed')
  } finally {
    submitting.value = false
  }
}
</script>
