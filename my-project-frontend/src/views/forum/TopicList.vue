<script setup>
import LightCard from "@/components/LightCard.vue";
import {
  Calendar,
  Clock,
  CollectionTag,
  Compass,
  Document,
  Edit,
  EditPen,
  Link,
  Picture,
  Microphone,
  CircleCheck,
  Star,
  FolderOpened,
  ArrowRightBold
} from "@element-plus/icons-vue";
import Weather from "@/components/Weather.vue";
import {computed, onMounted, reactive, ref, watch} from "vue";
import {ElMessage} from "element-plus";
import TopicEditor from "@/components/TopicEditor.vue";
import {useStore} from "@/store";
import ColorDot from "@/components/ColorDot.vue";
import router from "@/router";
import TopicTag from "@/components/TopicTag.vue";
import TopicCollectList from "@/components/TopicCollectList.vue";
import {apiForumTopicList, apiForumTopTopics, apiForumWeather} from "@/net/api/forum";

const store = useStore()

const weather = reactive({
  location: {},
  now: {},
  hourly: [],
  success: false
})
const editor = ref(false)
const topics = reactive({
  list: [],
  type: 0,
  page: 0,
  end: false,
  top: []
})
const collects = ref(false)

watch(() => topics.type, () => resetList(), {immediate: true})

const today = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
})

function updateList(){
  if(topics.end) return
  apiForumTopicList(topics.page, topics.type, data => {
    if(data) {
      data.forEach(d => topics.list.push(d))
      topics.page++
    }
    if(!data || data.length < 10)
      topics.end = true
  })
}

function onTopicCreate() {
  editor.value = false
  resetList()
}

function resetList() {
  topics.page = 0
  topics.end = false
  topics.list = []
  updateList()
}

navigator.geolocation.getCurrentPosition(position => {
  const longitude = position.coords.longitude
  const latitude = position.coords.latitude
  apiForumWeather(longitude, latitude, data => {
    Object.assign(weather, data)
    weather.success = true
  })
}, error => {
  console.info(error)
  ElMessage.warning('Location request timed out, please check network settings')
  apiForumWeather(116.40529, 39.90499, data => {
    Object.assign(weather, data)
    weather.success = true
  })
}, {
  timeout: 3000,
  enableHighAccuracy: true
})

onMounted(() => {
  apiForumTopTopics(data => topics.top = data)
})
</script>

<template>
  <div class="forum-page">
    <div class="forum-grid">
      <div class="forum-main">
        <light-card class="compose-card">
          <div class="compose-header">
            <div class="compose-title">
              <el-icon><EditPen/></el-icon>
              <span>Start a new conversation</span>
            </div>
            <el-button type="primary" round plain @click="editor = true">
              Create Topic
            </el-button>
          </div>
          <div class="compose-actions">
            <div class="compose-action">
              <el-icon><Edit /></el-icon>
              Text
            </div>
            <div class="compose-action">
              <el-icon><Document /></el-icon>
              Notes
            </div>
            <div class="compose-action">
              <el-icon><Compass /></el-icon>
              Discovery
            </div>
            <div class="compose-action">
              <el-icon><Picture /></el-icon>
              Photos
            </div>
            <div class="compose-action">
              <el-icon><Microphone /></el-icon>
              Audio
            </div>
          </div>
        </light-card>

        <light-card v-if="topics.top.length" class="pinned-card">
          <div class="section-title">
            <span class="title-label">Pinned Highlights</span>
          </div>
          <div class="pinned-list">
            <div v-for="item in topics.top" :key="item.id" class="pinned-item"
                 @click="router.push(`/index/topic-detail/${item.id}`)">
              <el-tag size="small" type="info">Pinned</el-tag>
              <div class="pinned-meta">
                <div class="pinned-name">{{item.title}}</div>
                <div class="pinned-time">{{new Date(item.time).toLocaleDateString()}}</div>
              </div>
            </div>
          </div>
        </light-card>

        <light-card class="filter-card">
          <div class="section-title">
            <span class="title-label">Browse by topic type</span>
          </div>
          <div class="type-select">
            <div
              v-for="item in store.forum.types"
              :key="item.id"
              :class="['type-chip', { active: topics.type === item.id }]"
              @click="topics.type = item.id"
            >
              <color-dot :color="item.color" />
              <span>{{ item.name }}</span>
            </div>
          </div>
        </light-card>

        <transition name="el-fade-in" mode="out-in">
          <div v-if="topics.list.length" class="topic-stream" v-infinite-scroll="updateList">
            <light-card v-for="item in topics.list" :key="item.id" class="topic-card"
                        @click="router.push('/index/topic-detail/'+item.id)">
              <div class="topic-header">
                <el-avatar :size="36" :src="store.avatarUserUrl(item.avatar)"/>
                <div class="topic-author">
                  <div class="topic-author-name">{{item.username}}</div>
                  <div class="topic-timestamp">
                    <el-icon><Clock/></el-icon>
                    <span>{{new Date(item.time).toLocaleString()}}</span>
                  </div>
                </div>
              </div>
              <div class="topic-title-row">
                <topic-tag :type="item.type"/>
                <span class="topic-title">{{item.title}}</span>
              </div>
              <div class="topic-content">{{item.text}}</div>
              <div v-if="item.images && item.images.length" class="topic-media">
                <el-image class="topic-image" v-for="img in item.images" :key="img" :src="img" fit="cover"></el-image>
              </div>
              <div class="topic-footer">
                <div class="topic-stat">
                  <el-icon><CircleCheck/></el-icon>
                  <span>{{item.like}} likes</span>
                </div>
                <div class="topic-stat">
                  <el-icon><Star/></el-icon>
                  <span>{{item.collect}} bookmarks</span>
                </div>
              </div>
            </light-card>
          </div>
        </transition>
      </div>

      <aside class="forum-aside">
        <div class="aside-sticky">
          <light-card class="bookmark-card">
            <div class="collect-list-button" @click="collects = true">
              <span><el-icon><FolderOpened /></el-icon> View My Bookmarks</span>
              <el-icon class="collect-arrow"><ArrowRightBold/></el-icon>
            </div>
          </light-card>

          <light-card class="announcement-card">
            <div class="section-title">
              <el-icon><CollectionTag/></el-icon>
              <span class="title-label">Forum Announcements</span>
            </div>
            <p>
              To foster a thoughtful academic atmosphere for the university's 70th anniversary, the National University of
              Defense Technology will host a cross-disciplinary forum in Changsha from November 24 to 26, 2022.
            </p>
          </light-card>

          <light-card class="weather-card">
            <div class="section-title">
              <el-icon><Calendar/></el-icon>
              <span class="title-label">Weather</span>
            </div>
            <weather :data="weather"/>
          </light-card>

          <light-card class="info-card">
            <div class="info-text">
              <div>Current Date</div>
              <div>{{today}}</div>
            </div>
            <div class="info-text">
              <div>Current IP Address</div>
              <div>127.0.0.1</div>
            </div>
          </light-card>

          <div class="links-heading">
            <el-icon><Link/></el-icon>
            <span>Friend Links</span>
          </div>
          <div class="friend-links">
            <div class="friend-link">
              <el-image style="height: 100%" src="https://element-plus.org/images/js-design-banner.jpg"/>
            </div>
            <div class="friend-link">
              <el-image style="height: 100%" src="https://element-plus.org/images/vform-banner.png"/>
            </div>
          </div>
        </div>
      </aside>
    </div>
    <topic-editor :show="editor" @success="onTopicCreate" @close="editor = false"/>
    <topic-collect-list :show="collects" @close="collects = false"/>
  </div>
</template>

<style lang="less" scoped>
.forum-page {
  width: 100%;
  padding: 32px 0 48px;
  display: flex;
  justify-content: center;
  background: radial-gradient(circle at top, rgba(255, 255, 255, 0.65), rgba(230, 236, 245, 0.6));
}

.forum-grid {
  width: min(1120px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 24px;
  padding: 0 32px;
  box-sizing: border-box;
}

.forum-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.forum-aside {
  width: 100%;
}

.aside-sticky {
  position: sticky;
  top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.compose-card {
  padding: 24px;
  backdrop-filter: blur(28px);
  background: rgba(255, 255, 255, 0.7);
}

.compose-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.compose-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.2px;
}

.compose-actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.compose-action {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.6);
  color: rgba(20, 20, 20, 0.6);
  transition: all 0.3s ease;

  &:hover {
    color: rgba(0, 0, 0, 0.8);
    box-shadow: 0 12px 24px rgba(15, 46, 80, 0.08);
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
  color: rgba(15, 23, 42, 0.7);
  letter-spacing: 0.3px;
}

.pinned-card {
  padding: 20px 24px;
}

.pinned-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.pinned-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 16px;
  transition: all 0.3s ease;

  &:hover {
    cursor: pointer;
    background: rgba(255, 255, 255, 0.72);
    box-shadow: 0 14px 30px rgba(15, 23, 42, 0.08);
  }
}

.pinned-meta {
  display: flex;
  justify-content: space-between;
  flex: 1;
  align-items: center;
}

.pinned-name {
  font-size: 15px;
  font-weight: 600;
}

.pinned-time {
  font-size: 13px;
  color: rgba(100, 116, 139, 0.85);
}

.filter-card {
  padding: 24px;
}

.type-select {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}

.type-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid transparent;
  font-size: 14px;
  color: rgba(15, 23, 42, 0.72);
  transition: all 0.3s ease;

  &:hover {
    cursor: pointer;
    transform: translateY(-1px);
    box-shadow: 0 12px 20px rgba(15, 23, 42, 0.08);
  }

  &.active {
    border-color: rgba(120, 144, 255, 0.5);
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(223, 233, 255, 0.9));
  }
}

.topic-stream {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.topic-card {
  padding: 24px;
  border-radius: 26px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    cursor: pointer;
    box-shadow: 0 22px 45px rgba(15, 23, 42, 0.12);
  }
}

.topic-header {
  display: flex;
  gap: 12px;
  align-items: center;
}

.topic-author {
  display: flex;
  flex-direction: column;
}

.topic-author-name {
  font-weight: 600;
  font-size: 15px;
  color: rgba(15, 23, 42, 0.9);
}

.topic-timestamp {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(100, 116, 139, 0.85);
}

.topic-title-row {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 16px;
}

.topic-title {
  font-size: 18px;
  font-weight: 600;
  color: rgba(15, 23, 42, 0.95);
}

.topic-content {
  margin-top: 12px;
  font-size: 14px;
  line-height: 1.6;
  color: rgba(71, 85, 105, 0.9);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

.topic-media {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.topic-image {
  width: 100%;
  height: 120px;
  border-radius: 14px;
  object-fit: cover;
}

.topic-footer {
  display: flex;
  gap: 24px;
  margin-top: 18px;
  font-size: 13px;
  color: rgba(71, 85, 105, 0.9);
}

.topic-stat {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bookmark-card {
  padding: 18px 22px;
}

.collect-list-button {
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;

  &:hover {
    cursor: pointer;
    opacity: 0.7;
  }
}

.collect-arrow {
  transform: translateY(1px);
}

.announcement-card {
  padding: 22px 24px;
  line-height: 1.6;
  color: rgba(71, 85, 105, 0.9);
}

.weather-card {
  padding: 22px 24px;
}

.info-card {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-text {
  display: flex;
  justify-content: space-between;
  color: rgba(71, 85, 105, 0.9);
  font-size: 14px;
}

.links-heading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(71, 85, 105, 0.9);
  font-size: 14px;
  font-weight: 600;
  margin-top: 8px;
}

.friend-links {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.friend-link {
  border-radius: 18px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.7);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.dark {
  .compose-card,
  .filter-card,
  .bookmark-card,
  .announcement-card,
  .weather-card,
  .info-card,
  .pinned-card {
    background: rgba(24, 24, 24, 0.72);
    backdrop-filter: blur(28px);
  }

  .compose-action,
  .type-chip {
    background: rgba(56, 56, 56, 0.65);
    color: rgba(231, 231, 231, 0.8);
  }

  .type-chip.active {
    border-color: rgba(148, 163, 255, 0.4);
    background: linear-gradient(135deg, rgba(80, 80, 80, 0.9), rgba(62, 62, 62, 0.9));
  }

  .topic-card {
    background: rgba(28, 28, 28, 0.78);
  }

  .topic-author-name,
  .topic-title,
  .pinned-name,
  .links-heading {
    color: rgba(244, 244, 244, 0.92);
  }

  .topic-content,
  .topic-stat span,
  .pinned-time,
  .info-text,
  .announcement-card,
  .collect-list-button,
  .section-title,
  .topic-timestamp,
  .links-heading {
    color: rgba(203, 213, 225, 0.85);
  }

  .forum-page {
    background: radial-gradient(circle at top, rgba(32, 32, 32, 0.82), rgba(12, 12, 12, 0.9));
  }
}

@media (max-width: 1100px) {
  .forum-grid {
    grid-template-columns: 1fr;
    padding: 0 18px;
  }

  .forum-aside {
    order: -1;
  }
}
</style>
