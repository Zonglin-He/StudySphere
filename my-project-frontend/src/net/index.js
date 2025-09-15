import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router";

const authItemName = "authorize"

const accessHeader = () => {
    return {
        'Authorization': `Bearer ${takeAccessToken()?.token}`
    }
}

const defaultError = (error) => {
    console.error(error)
    if (error.response && error.response.status) {
        const status = error.response.status
        if (status === 429) {
            ElMessage.error(error.response.data.message)
        } else {
            ElMessage.error('An unexpected error occurred, please contact the administrator')
        }
    } else {
        ElMessage.error('Backend server is not available')
    }
}

const defaultFailure = (message, status, url) => {
    console.warn(`Request URL: ${url}, Status Code: ${status}, Error Message: ${message}`)
    ElMessage.warning(message)
}

function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    if(!str) return null
    try {
        const authObj = JSON.parse(str)
        if(new Date(authObj.expire) <= new Date()) {
            deleteAccessToken()
            // Don't show expired message on initial load - only show when actually expired during use
            return null
        }
        return authObj
    } catch (e) {
        // Invalid token format, clear it
        deleteAccessToken()
        return null
    }
}

function storeAccessToken(remember, token, expire, role){
    const authObj = { token, expire, role }
    const str = JSON.stringify(authObj)
    if(remember)
        localStorage.setItem(authItemName, str)
    else
        sessionStorage.setItem(authItemName, str)
}

function deleteAccessToken(redirect = false) {
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
    if(redirect) {
        router.push({ name: 'welcome-login' })
    }
}

function internalPost(url, data, headers, success, failure, error = defaultError){
    axios.post(url, data, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else if(data.code === 401) {
            failure('Login session has expired, please log in again!')
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function internalGet(url, headers, success, failure, error = defaultError){
    axios.get(url, { headers: headers }).then(({data}) => {
        if(data.code === 200) {
            success(data.data)
        } else if(data.code === 401) {
            failure('Login session has expired, please log in again!')
            deleteAccessToken(true)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => error(err))
}

function login(username, password, remember, success, failure = defaultFailure){
    const params = new URLSearchParams({ username, password })
    internalPost('/api/auth/login', params, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        storeAccessToken(remember, data.token, data.expire, data.role)
        ElMessage.success(`Login successful, welcome ${data.username} to our system`)
        success(data)
    }, failure)
}

function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader() , success, failure)
}

function logout(success, failure = defaultFailure){
    get('/api/auth/logout', () => {
        deleteAccessToken()
        ElMessage.success(`Logout successful, see you again!`)
        success()
    }, failure)
}

function get(url, success, failure = defaultFailure) {
    internalGet(url, accessHeader(), success, failure)
}

function isUnauthorized() {
    return !takeAccessToken()
}

function isRoleAdmin() {
    return takeAccessToken()?.role === 'admin'
}

export { post, get, login, logout, isUnauthorized, isRoleAdmin, accessHeader }
