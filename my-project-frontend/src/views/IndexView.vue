<script setup>
import {inject, reactive, ref} from "vue";
import {
  Bell,
  ChatDotSquare, Check, Collection, DataLine,
  Document, Files,
  Location, Lock, Monitor,
  Notification, Operation,
  Position,
  School, Search,
  Umbrella, User
} from "@element-plus/icons-vue";
import LightCard from "@/components/LightCard.vue";
import UserInfo from "@/components/UserInfo.vue";
import {apiNotificationDelete, apiNotificationDeleteAll, apiNotificationList} from "@/net/api/user";

const userMenu = [
  {
    title: 'Campus Forum', icon: Location, sub: [
      { title: 'Forum Square', icon: ChatDotSquare, index: '/index' },
      { title: 'Lost & Found', icon: Bell },
      { title: 'Campus Events', icon: Notification },
      { title: 'Confession Wall', icon: Umbrella },
      { title: 'Haiwen Postgrad', icon: School }
    ]
  }, {
    title: 'Explore & Discover', icon: Position, sub: [
      { title: 'Grade Inquiry', icon: Document },
      { title: 'Class Schedule', icon: Files },
      { title: 'Academic Notices', icon: Monitor },
      { title: 'Online Library', icon: Collection },
      { title: 'Classroom Booking', icon: DataLine }
    ]
  }, {
    title: 'Personal Settings', icon: Operation, sub: [
      { title: 'Profile Settings', icon: User, index: '/index/user-setting' },
      { title: 'Account Security', icon: Lock, index: '/index/privacy-setting' }
    ]
  }
]

const loading = inject('userLoading')

const searchInput = reactive({
  type: '1',
  text: ''
})
const notification = ref([])

const loadNotification =
    () => apiNotificationList(data => notification.value = data)
loadNotification()

function confirmNotification(id, url) {
  apiNotificationDelete(id, () => {
    loadNotification()
    window.open(url)
  })
}

function deleteAllNotification() {
  apiNotificationDeleteAll(loadNotification)
}
</script>

<template>
  <div class="main-content" v-loading="loading" element-loading-text="Entering, please wait...">
    <el-container class="workspace" v-if="!loading">
      <el-header class="main-content-header">
        <div class="header-inner">
          <div class="brand-area">
            <div class="traffic-lights" aria-hidden="true">
              <span class="dot red"></span>
              <span class="dot amber"></span>
              <span class="dot green"></span>
            </div>
            <div class="logo-title">
              <el-image class="logo" src="https://element-plus.org/images/element-plus-logo.svg" />
              <span class="brand-name">StudySphere</span>
            </div>
          </div>
          <div class="search-area">
            <el-input v-model="searchInput.text" placeholder="Search forum content...">
              <template #prefix>
                <el-icon>
                  <Search />
                </el-icon>
              </template>
              <template #append>
                <el-select v-model="searchInput.type">
                  <el-option value="1" label="Forum Square" />
                  <el-option value="2" label="Campus Events" />
                  <el-option value="3" label="Confession Wall" />
                  <el-option value="4" label="Academic Notices" />
                </el-select>
              </template>
            </el-input>
          </div>
          <user-info class="header-user">
            <el-popover placement="bottom" :width="350" trigger="click">
              <template #reference>
                <el-badge is-dot :hidden="!notification.length">
                  <div class="notification">
                    <el-icon><Bell /></el-icon>
                    <div class="notification-label">Messages</div>
                  </div>
                </el-badge>
              </template>
              <el-empty :image-size="80" description="No unread messages for now~" v-if="!notification.length" />
              <el-scrollbar :max-height="500" v-else>
                <light-card
                  v-for="item in notification"
                  :key="item.id"
                  class="notification-item"
                  @click="confirmNotification(item.id, item.url)"
                >
                  <div>
                    <el-tag size="small" :type="item.type">Message</el-tag>&nbsp;
                    <span class="notification-title">{{ item.title }}</span>
                  </div>
                  <el-divider class="notification-divider" />
                  <div class="notification-content">
                    {{ item.content }}
                  </div>
                </light-card>
              </el-scrollbar>
              <div class="notification-actions">
                <el-button
                  size="small"
                  type="info"
                  :icon="Check"
                  @click="deleteAllNotification"
                  plain
                >
                  Clear All Unread Messages
                </el-button>
              </div>
            </el-popover>
          </user-info>
        </div>
      </el-header>
      <el-container class="workspace-body">
        <el-aside width="240px" class="workspace-sidebar">
          <el-scrollbar class="sidebar-scroll">
            <el-menu
              class="nav-menu"
              router
              :default-active="$route.path"
              :default-openeds="['1', '2', '3']"
            >
              <el-sub-menu :index="(index + 1).toString()" v-for="(menu, index) in userMenu" :key="menu.title">
                <template #title>
                  <el-icon>
                    <component :is="menu.icon" />
                  </el-icon>
                  <span class="menu-title">{{ menu.title }}</span>
                </template>
                <el-menu-item :index="subMenu.index" v-for="subMenu in menu.sub" :key="subMenu.title">
                  <template #title>
                    <el-icon>
                      <component :is="subMenu.icon" />
                    </el-icon>
                    {{ subMenu.title }}
                  </template>
                </el-menu-item>
              </el-sub-menu>
            </el-menu>
          </el-scrollbar>
        </el-aside>
        <el-main class="main-content-page">
          <el-scrollbar class="page-scroll">
            <router-view v-slot="{ Component }">
              <transition name="el-fade-in-linear" mode="out-in">
                <component :is="Component" class="page-view" />
              </transition>
            </router-view>
          </el-scrollbar>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style lang="less" scoped>
.main-content {
  height: 100vh;
  width: 100%;
  padding: 36px 48px;
  box-sizing: border-box;
  display: flex;
  align-items: stretch;
  justify-content: center;
}

.workspace {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.68);
  backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 40px 80px rgba(31, 51, 73, 0.15);
  overflow: hidden;
  transition: background 0.3s ease, border 0.3s ease, box-shadow 0.3s ease;
}

.workspace-body {
  flex: 1;
}

.main-content-header {
  height: 88px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.45);
  padding: 0 36px;
  background: rgba(255, 255, 255, 0.38);
  backdrop-filter: blur(24px);
  transition: background 0.3s ease, border 0.3s ease;
}

.header-inner {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 32px;
}

.brand-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.traffic-lights {
  display: flex;
  gap: 8px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  box-shadow: inset 0 0 4px rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.dot.red { background: linear-gradient(135deg, #ff5f57, #e04845); }
.dot.amber { background: linear-gradient(135deg, #febc2e, #e0a624); }
.dot.green { background: linear-gradient(135deg, #28c840, #1da535); }

.logo-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  height: 32px;
  filter: drop-shadow(0 6px 12px rgba(50, 80, 120, 0.25));
}

.brand-name {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.4px;
}

.search-area {
  flex: 1;
  display: flex;
  justify-content: center;
}

.search-area :deep(.el-input) {
  max-width: 520px;
  width: 100%;
  --el-input-bg-color: rgba(255, 255, 255, 0.6);
  --el-input-border-color: rgba(255, 255, 255, 0.55);
  --el-input-hover-border-color: rgba(255, 255, 255, 0.7);
  --el-input-focus-border-color: rgba(79, 131, 255, 0.75);
  --el-input-border-radius: 16px 0 0 16px;
  backdrop-filter: blur(16px);
  transition: box-shadow 0.3s ease;
  box-shadow: 0 10px 24px rgba(53, 74, 118, 0.12);
}

.search-area :deep(.el-select) {
  --el-fill-color-blank: rgba(245, 247, 255, 0.7);
  --el-select-border-color-hover: rgba(79, 131, 255, 0.75);
  border-radius: 0 16px 16px 0;
  backdrop-filter: blur(16px);
}

.header-user {
  display: flex;
  justify-content: flex-end;
  flex: 0 0 320px;
}

.workspace-sidebar {
  padding: 24px 20px;
  border-right: 1px solid rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(24px);
}

.sidebar-scroll {
  height: 100%;
}

.nav-menu {
  background: transparent;
  border-right: none;
}

.nav-menu :deep(.el-sub-menu__title),
.nav-menu :deep(.el-menu-item) {
  height: 52px;
  line-height: 52px;
  border-radius: 14px;
  margin: 4px 0;
}

.nav-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(88, 132, 255, 0.9), rgba(144, 202, 249, 0.85));
  color: #fff;
  box-shadow: 0 12px 24px rgba(88, 132, 255, 0.25);
}

.nav-menu :deep(.el-menu-item:hover),
.nav-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(88, 132, 255, 0.15);
}

.menu-title {
  font-weight: 600;
  letter-spacing: 0.3px;
}

.main-content-page {
  padding: 32px 40px;
  background: rgba(255, 255, 255, 0.42);
  backdrop-filter: blur(32px);
}

.page-scroll {
  height: 100%;
}

.page-view {
  min-height: calc(100vh - 200px);
}

.notification {
  font-size: 22px;
  line-height: 16px;
  text-align: center;
  transition: color 0.3s ease, transform 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.notification-label {
  font-size: 11px;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  margin-top: 2px;
}

.notification:hover {
  color: rgba(88, 132, 255, 0.9);
  cursor: pointer;
  transform: translateY(-1px);
}

.notification-item {
  transition: transform 0.3s ease, opacity 0.3s ease;
  border-radius: 16px;
}

.notification-item:hover {
  cursor: pointer;
  opacity: 0.85;
  transform: translateY(-3px);
}

.notification-title {
  font-weight: 600;
}

.notification-divider {
  margin: 7px 0 6px;
}

.notification-content {
  font-size: 13px;
  color: #6f7785;
}

.notification-actions {
  margin-top: 12px;
}

.dark .workspace {
  background: rgba(22, 24, 33, 0.7);
  border-color: rgba(255, 255, 255, 0.08);
  box-shadow: 0 40px 80px rgba(4, 8, 20, 0.45);
}

.dark .main-content-header {
  border-bottom-color: rgba(255, 255, 255, 0.08);
  background: rgba(24, 26, 36, 0.6);
}

.dark .workspace-sidebar {
  border-right-color: rgba(255, 255, 255, 0.08);
  background: rgba(24, 26, 36, 0.55);
}

.dark .main-content-page {
  background: rgba(30, 33, 46, 0.6);
}

.dark .notification-content {
  color: #b7bdd2;
}

@media (max-width: 1200px) {
  .main-content {
    padding: 24px;
  }

  .header-inner {
    gap: 16px;
  }

  .header-user {
    flex-basis: 260px;
  }

  .main-content-page {
    padding: 24px;
  }
}

@media (max-width: 960px) {
  .main-content {
    padding: 12px;
  }

  .workspace {
    border-radius: 16px;
  }

  .main-content-header {
    padding: 0 20px;
  }

  .header-inner {
    flex-wrap: wrap;
    justify-content: center;
  }

  .header-user {
    flex: 1 1 100%;
    justify-content: center;
  }

  .workspace-sidebar {
    display: none;
  }
}
</style>
