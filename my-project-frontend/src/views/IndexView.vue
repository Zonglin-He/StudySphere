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
      <el-header class="main-content-header">
        <div style="width: 320px;height: 32px">
          <el-image class="logo" src="https://element-plus.org/images/element-plus-logo.svg"/>
        </div>
        <div style="flex: 1;padding: 0 20px;text-align: center">
          <el-input v-model="searchInput.text" style="width: 100%;max-width: 500px"
                    placeholder="Search forum content...">
            <template #prefix>
              <el-icon>
                <Search/>
              </el-icon>
            </template>
            <template #append>
              <el-select style="width: 120px" v-model="searchInput.type">
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
      <el-container>
        <el-aside width="230px">
          <el-scrollbar style="height: calc(100vh - 55px)">
            <el-menu
                router
                :default-active="$route.path"
                :default-openeds="['1', '2', '3']"
                style="min-height: calc(100vh - 55px)">
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
          <el-scrollbar style="height: calc(100vh - 55px)">
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

.main-content-page {
  padding: 0;
  background-color: #f7f8fa;
}

.dark .main-content-page {
  background-color: #212225;
}

.main-content {
  height: 100vh;
  width: 100vw;
}

.main-content-header {
  border-bottom: solid 1px var(--el-border-color);
  height: 55px;
  display: flex;
  align-items: center;
  box-sizing: border-box;

  .logo {
    height: 32px;
  }

  .user-info {
    display: flex;
    justify-content: flex-end;
    align-items: center;

    .el-avatar:hover {
      cursor: pointer;
    }

    .profile {
      text-align: right;
      margin-right: 20px;

      :first-child {
        font-size: 18px;
        font-weight: bold;
        line-height: 20px;
      }

      :last-child {
        font-size: 10px;
        color: grey;
      }
    }
  }
}
</style>
