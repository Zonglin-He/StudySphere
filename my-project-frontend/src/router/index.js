import { createRouter, createWebHistory } from 'vue-router'
import { isRoleAdmin, isUnauthorized } from '@/net'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    // 显式 login 子路由；alias 让 "/" 也渲染它
                    path: 'login',
                    alias: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue'),
                },
                {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue'),
                },
                {
                    path: 'forget',
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue'),
                },
            ],
        },
        {
            path: '/index',
            name: 'index',
            component: () => import('@/views/IndexView.vue'),
            children: [
                {
                    path: '',
                    name: 'topics',
                    component: () => import('@/views/forum/Forum.vue'),
                    children: [
                        { path: '', name: 'topic-list', component: () => import('@/views/forum/TopicList.vue') },
                        { path: 'topic-detail/:tid', name: 'topic-detail', component: () => import('@/views/forum/TopicDetail.vue') },
                    ],
                },
                { path: 'user-setting', name: 'user-setting', component: () => import('@/views/settings/UserSetting.vue') },
                { path: 'privacy-setting', name: 'privacy-setting', component: () => import('@/views/settings/PrivacySetting.vue') },
            ],
        },
        {
            path: '/admin',
            name: 'admin',
            component: () => import('@/views/AdminView.vue'),
            children: [
                { path: '', name: 'admin-welcome', component: () => import('@/views/admin/WelcomeAdmin.vue') },
                { path: 'user', name: 'admin-user', component: () => import('@/views/admin/UserAdmin.vue') },
                { path: 'forum', name: 'admin-forum', component: () => import('@/views/admin/ForumAdmin.vue') },
            ],
        },
        // 兜底：未知路径回到登录
        { path: '/:pathMatch(.*)*', redirect: '/login' },
    ],
})

router.beforeEach((to, _from, next) => {
    const unauthorized = isUnauthorized()
    const admin = isRoleAdmin()
    const name = String(to.name ?? '')
    const path = to.path

    if (name.startsWith('welcome') && !unauthorized) return next('/index')
    if (path.startsWith('/admin') && !admin)        return next('/index')
    if (path.startsWith('/index') && unauthorized)  return next('/login') // 明确指到 /login
    next()
})

export default router
