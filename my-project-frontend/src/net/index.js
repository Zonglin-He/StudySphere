// src/net/index.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const AUTH_KEY = 'authorize'

// -------- helpers --------
function normalizeBearer(token) {
    if (!token) return ''
    return token.startsWith('Bearer ') ? token : `Bearer ${token}`
}

function parseExpire(expire) {
    if (typeof expire === 'number') return expire
    if (typeof expire === 'string') {
        const iso = expire.includes(' ') ? expire.replace(' ', 'T') : expire
        const d = new Date(iso)
        if (!Number.isNaN(d.getTime())) return d.getTime()
    }
    return 0
}

function saveAuth(remember, rawToken, expire, role) {
    const token = normalizeBearer(rawToken)
    const expMs = parseExpire(expire)
    const obj = { token, expire: expMs, role }
    const str = JSON.stringify(obj)
    if (remember) localStorage.setItem(AUTH_KEY, str)
    else sessionStorage.setItem(AUTH_KEY, str)
    axios.defaults.headers.common.Authorization = token
}

function loadAuth() {
    const str = localStorage.getItem(AUTH_KEY) || sessionStorage.getItem(AUTH_KEY)
    if (!str) return null
    try {
        const obj = JSON.parse(str)
        if (obj.expire && Date.now() >= Number(obj.expire)) {
            clearAuth()
            return null
        }
        obj.token = normalizeBearer(obj.token)
        return obj
    } catch {
        clearAuth()
        return null
    }
}

function clearAuth(redirect) {
    localStorage.removeItem(AUTH_KEY)
    sessionStorage.removeItem(AUTH_KEY)
    delete axios.defaults.headers.common.Authorization
    if (redirect) router.push({ name: 'welcome-login' })
}

function authHeader() {
    const a = loadAuth()
    return a && a.token ? { Authorization: a.token } : {}
}

// -------- defaults --------
function defaultError(err) {
    console.error(err)
    const status = err && err.response && err.response.status
    if (status === 429) {
        ElMessage.error(err.response.data?.message || 'Too many requests')
    } else if (status) {
        ElMessage.error('An unexpected error occurred, please contact the administrator')
    } else {
        ElMessage.error('Backend server is not available')
    }
}

function defaultFailure(message, status, url) {
    console.warn(`Request URL: ${url}, Status Code: ${status}, Error Message: ${message}`)
    ElMessage.warning(message)
}

// -------- thin axios wrappers --------
function _post(url, data, headers, ok, fail, err) {
    fail = fail || defaultFailure
    err = err || defaultError
    axios.post(url, data, { headers }).then(({ data }) => {
        if (data.code === 200) ok(data.data)
        else if (data.code === 401) {
            fail('Login session has expired, please log in again!', 401, url)
            clearAuth(true)
        } else {
            fail(data.message, data.code, url)
        }
    }).catch(err)
}

function _get(url, headers, ok, fail, err) {
    fail = fail || defaultFailure
    err = err || defaultError
    axios.get(url, { headers }).then(({ data }) => {
        if (data.code === 200) ok(data.data)
        else if (data.code === 401) {
            fail('Login session has expired, please log in again!', 401, url)
            clearAuth(true)
        } else {
            fail(data.message, data.code, url)
        }
    }).catch(err)
}

// -------- exported APIs --------
export function login(username, password, remember, onSuccess, onFail) {
    onFail = onFail || defaultFailure
    // 注意：这里不要再写 /api；axios.defaults.baseURL 已经设为 '/api'
    const body = new URLSearchParams({ username, password })
    _post('/auth/login', body, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        saveAuth(remember, data.token, data.expire, data.role)
        ElMessage.success(`Login successful, welcome ${data.username}!`)
        onSuccess && onSuccess(data)
    }, onFail)
}

export function logout(onSuccess, onFail) {
    onFail = onFail || defaultFailure
    _get('/auth/logout', authHeader(), () => {
        clearAuth()
        ElMessage.success('Logout successful, see you again!')
        onSuccess && onSuccess()
    }, onFail)
}

export function post(url, data, ok, fail) {
    _post(url, data, authHeader(), ok, fail)
}

export function get(url, ok, fail) {
    _get(url, authHeader(), ok, fail)
}

export function isUnauthorized() {
    return !loadAuth()
}

export function isRoleAdmin() {
    const a = loadAuth()
    return a && a.role === 'admin'
}

export { authHeader as accessHeader, clearAuth as deleteAccessToken, saveAuth as storeAccessToken, loadAuth }
