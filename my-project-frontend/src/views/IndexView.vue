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
    <el-container style="height: 100%" v-if="!loading">
      <el-header class="main-content-header glass-header">
        <div class="brand">
          <div class="brand-icon"></div>
          <div class="brand-copy">
            <div class="brand-title">StudySphere</div>
            <div class="brand-subtitle">Designed with an Apple-inspired touch</div>
          </div>
        </div>
        <div class="search-area">
          <el-input v-model="searchInput.text" class="search-input"
                    placeholder="Search across your campus universe...">
            <template #prefix>
              <el-icon>
                <Search/>
              </el-icon>
            </template>
            <template #append>
              <el-select class="search-filter" v-model="searchInput.type">
                <el-option value="1" label="Forum Square"/>
                <el-option value="2" label="Campus Events"/>
                <el-option value="3" label="Confession Wall"/>
                <el-option value="4" label="Academic Notices"/>
              </el-select>
            </template>
          </el-input>
        </div>
        <user-info>
          <el-popover placement="bottom" :width="350" trigger="click">
            <template #reference>
              <el-badge is-dot :hidden="!notification.length">
                <div class="notification">
                  <el-icon><Bell/></el-icon>
                  <div style="font-size: 10px">Messages</div>
                </div>
              </el-badge>
            </template>
            <el-empty :image-size="80" description="No unread messages for now~" v-if="!notification.length"/>
            <el-scrollbar :max-height="500" v-else>
              <light-card v-for="item in notification" class="notification-item"
                          @click="confirmNotification(item.id, item.url)">
                <div>
                  <el-tag size="small" :type="item.type">Message</el-tag>&nbsp;
                  <span style="font-weight: bold">{{item.title}}</span>
                </div>
                <el-divider style="margin: 7px 0 3px 0"/>
                <div style="font-size: 13px;color: grey">
                  {{item.content}}
                </div>
              </light-card>
            </el-scrollbar>
            <div style="margin-top: 10px">
              <el-button size="small" type="info" :icon="Check" @click="deleteAllNotification"
                         style="width: 100%" plain>Clear All Unread Messages</el-button>
            </div>
          </el-popover>
        </user-info>
      </el-header>
      <el-container class="content-body">
        <el-aside width="250px" class="main-aside glass-panel">
          <el-scrollbar class="aside-scroll">
            <el-menu
                router
                :default-active="$route.path"
                :default-openeds="['1', '2', '3']"
                class="aside-menu">
              <el-sub-menu :index="(index + 1).toString()"
                           v-for="(menu, index) in userMenu">
                <template #title>
                  <el-icon>
                    <component :is="menu.icon"/>
                  </el-icon>
                  <span><b>{{ menu.title }}</b></span>
                </template>
                <el-menu-item :index="subMenu.index" v-for="subMenu in menu.sub">
                  <template #title>
                    <el-icon>
                      <component :is="subMenu.icon"/>
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
                <component :is="Component" style="height: 100%"/>
              </transition>
            </router-view>
          </el-scrollbar>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style lang="less" scoped>
.notification-item {
  transition: .3s;
  &:hover {
    cursor: pointer;
    opacity: 0.7;
  }
}

.notification {
  font-size: 22px;
  line-height: 14px;
  text-align: center;
  transition: color .3s;

  &:hover {
    color: grey;
    cursor: pointer;
  }
}

.main-content {
  height: 100vh;
  width: 100vw;
  position: relative;
  background: linear-gradient(160deg, rgba(244, 247, 255, 0.88) 0%, #ffffff 45%, rgba(233, 238, 255, 0.92) 100%);
  overflow: hidden;
}

.main-content::before {
  content: "";
  position: absolute;
  top: -220px;
  right: -160px;
  width: 520px;
  height: 520px;
  background: radial-gradient(circle at center, rgba(99, 102, 241, 0.55), rgba(59, 130, 246, 0.05) 72%);
  filter: blur(70px);
  opacity: 0.8;
  pointer-events: none;
}

.dark .main-content {
  background: radial-gradient(circle at top, rgba(11, 17, 33, 0.95), rgba(9, 11, 19, 0.98) 65%, rgba(4, 6, 12, 1));
}

.dark .main-content::before {
  background: radial-gradient(circle at center, rgba(99, 102, 241, 0.45), rgba(15, 23, 42, 0.1) 70%);
  opacity: 0.6;
}

.glass-header {
  border-bottom: none;
  height: 84px;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  margin: 32px 36px 0;
  padding: 0 32px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(28px) saturate(140%);
  box-shadow: 0 24px 40px rgba(15, 23, 42, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.65);
  gap: 24px;
}

.dark .glass-header {
  background: rgba(24, 32, 52, 0.72);
  border: 1px solid rgba(148, 163, 255, 0.18);
  box-shadow: 0 28px 46px rgba(2, 6, 23, 0.65);
}

.brand {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 260px;
}

.brand-icon {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  background: linear-gradient(145deg, #6366f1, #8b5cf6);
  box-shadow: 0 14px 24px rgba(99, 102, 241, 0.35);
  position: relative;
}

.brand-icon::after {
  content: "";
  position: absolute;
  inset: 10px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  filter: blur(4px);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand-title {
  font-size: 1.4rem;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: #1e1f25;
}

.brand-subtitle {
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  color: rgba(30, 31, 37, 0.55);
}

.dark .brand-title {
  color: rgba(255, 255, 255, 0.92);
}

.dark .brand-subtitle {
  color: rgba(226, 232, 240, 0.55);
}

.search-area {
  flex: 1;
  display: flex;
  justify-content: center;
}

.search-input {
  width: 100%;
  max-width: 520px;
  --el-input-bg-color: rgba(255, 255, 255, 0.65);
  --el-input-border-color: transparent;
  --el-input-hover-border-color: rgba(99, 102, 241, 0.45);
  --el-input-focus-border-color: rgba(99, 102, 241, 0.65);
  border-radius: 999px;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.65);
  overflow: hidden;
}

.search-filter {
  width: 150px;
  background: transparent;
  border: none;
}

.content-body {
  padding: 28px 36px 36px;
  display: flex;
  gap: 28px;
}

.main-aside {
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(28px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 24px 50px rgba(15, 23, 42, 0.15);
  padding: 24px 0;
}

.aside-scroll {
  height: calc(100vh - 210px);
  padding: 0 8px;
}

.aside-menu {
  min-height: calc(100vh - 210px);
  background: transparent;
  border-right: none;
  padding: 0 16px;
}

.aside-menu .el-menu-item {
  border-radius: 18px;
  margin: 6px 0;
  height: 44px;
  line-height: 44px;
}

.aside-menu .el-sub-menu__title {
  height: 48px;
  line-height: 48px;
}

.main-content-page {
  padding: 0;
  background: rgba(255, 255, 255, 0.86);
  border-radius: 36px;
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.7);
}

.page-scroll {
  height: calc(100vh - 210px);
  padding: 0 12px 12px;
}

.dark .main-aside {
  background: rgba(24, 32, 52, 0.72);
  border: 1px solid rgba(148, 163, 255, 0.18);
  box-shadow: 0 24px 48px rgba(2, 6, 23, 0.65);
}

.dark .aside-menu {
  background: transparent;
}

.dark .main-content-page {
  background: rgba(24, 32, 52, 0.78);
  border: 1px solid rgba(148, 163, 255, 0.2);
  box-shadow: 0 30px 60px rgba(2, 6, 23, 0.7);
}

.dark .search-input {
  --el-input-bg-color: rgba(24, 32, 52, 0.65);
  box-shadow: inset 0 0 0 1px rgba(148, 163, 255, 0.18);
}

.dark .brand-icon::after {
  background: rgba(255, 255, 255, 0.35);
}

.main-content-header .user-info {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.main-content-header .user-info .el-avatar:hover {
  cursor: pointer;
}

.main-content-header .user-info .profile {
  text-align: right;
  margin-right: 20px;
}

.main-content-header .user-info .profile :first-child {
  font-size: 18px;
  font-weight: bold;
  line-height: 20px;
}

.main-content-header .user-info .profile :last-child {
  font-size: 10px;
  color: grey;
}

@media (max-width: 1280px) {
  .glass-header {
    flex-wrap: wrap;
    height: auto;
    padding: 18px 24px;
    gap: 18px;
  }

  .brand {
    min-width: 100%;
  }

  .search-area {
    width: 100%;
    order: 3;
  }

  .content-body {
    flex-direction: column;
  }

  .main-aside {
    width: 100% !important;
  }
}

@media (max-width: 768px) {
  .main-content {
    background: linear-gradient(175deg, rgba(244, 247, 255, 0.92), #ffffff 70%);
  }

  .glass-header {
    margin: 24px 18px 0;
  }

  .content-body {
    padding: 24px 18px 28px;
  }

  .page-scroll,
  .aside-scroll {
    height: auto;
    max-height: none;
  }
}
</style>
