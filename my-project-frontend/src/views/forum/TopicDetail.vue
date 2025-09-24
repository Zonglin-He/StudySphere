<script setup>
import {useRoute} from "vue-router";
import {reactive, ref} from "vue";
import {ArrowLeft, ChatSquare, CircleCheck, Delete, EditPen, Female, Male, Plus, Star} from "@element-plus/icons-vue";
import { QuillDeltaToHtmlConverter } from 'quill-delta-to-html';
import LightCard from "@/components/LightCard.vue";
import router from "@/router";
import TopicTag from "@/components/TopicTag.vue";
import InteractButton from "@/components/InteractButton.vue";
import {ElMessage} from "element-plus";
import {useStore} from "@/store";
import TopicEditor from "@/components/TopicEditor.vue";
import TopicCommentEditor from "@/components/TopicCommentEditor.vue";
import {
  apiForumCommentDelete,
  apiForumComments,
  apiForumInteract,
  apiForumTopic,
  apiForumUpdateTopic
} from "@/net/api/forum";

const route = useRoute()
const store = useStore()

const tid = route.params.tid

const topic = reactive({
  data: null,
  like: false,
  collect: false,
  comments: null,
  page: 1
})
const edit = ref(false)
const comment = reactive({
  show: false,
  text: '',
  quote: null
})

const init = () => apiForumTopic(tid, data => {
  topic.data = data
  topic.like = data.interact.like
  topic.collect = data.interact.collect
  loadComments(1)
})
init()

function convertToHtml(content) {
  const ops = JSON.parse(content).ops
  const converter = new QuillDeltaToHtmlConverter(ops, { inlineStyles: true });
  return converter.convert();
}

function interact(type, message) {
  apiForumInteract(tid, type, topic, message)
}

function updateTopic(editor) {
  apiForumUpdateTopic({
    id: tid,
    type: editor.type.id,
    title: editor.title,
    content: editor.text
  }, () => {
    ElMessage.success('Post content updated successfully!')
    edit.value = false
    init()
  })
}

function loadComments(page) {
  topic.comments = null
  topic.page = page
  apiForumComments(tid, page - 1, data => topic.comments = data)
}

function onCommentAdd() {
  comment.show = false
  loadComments(Math.floor(++topic.data.comments / 10) + 1)
}

function deleteComment(id) {
  apiForumCommentDelete(id, () => {
    ElMessage.success('Comment deleted successfully!')
    loadComments(topic.page)
  })
}
</script>

<template>
  <div class="topic-detail-page" v-if="topic.data">
    <div class="topic-detail-shell">
      <light-card class="topic-toolbar">
        <el-button :icon="ArrowLeft" type="info" size="small"
                   plain round @click="router.push('/index')">Back to Forum</el-button>
        <div class="toolbar-title">
          <topic-tag :type="topic.data.type"/>
          <span>{{topic.data.title}}</span>
        </div>
      </light-card>

      <div class="topic-main">
        <light-card class="profile-card">
          <div class="profile-header">
            <el-avatar :src="store.avatarUserUrl(topic.data.user.avatar)" :size="68"/>
            <div class="profile-info">
              <div class="profile-name">
                {{topic.data.user.username}}
                <span class="gender" v-if="topic.data.user.gender === 1">
                  <el-icon><Female/></el-icon>
                </span>
                <span class="gender male" v-if="topic.data.user.gender === 0">
                  <el-icon><Male/></el-icon>
                </span>
              </div>
              <div class="profile-email">{{topic.data.user.email}}</div>
            </div>
          </div>
          <el-divider class="profile-divider"/>
          <div class="profile-contact">
            <div><span>WeChat</span><span>{{topic.data.user.wx || 'Hidden or not provided'}}</span></div>
            <div><span>QQ</span><span>{{topic.data.user.qq || 'Hidden or not provided'}}</span></div>
            <div><span>Phone</span><span>{{topic.data.user.phone || 'Hidden or not provided'}}</span></div>
          </div>
          <el-divider class="profile-divider"/>
          <div class="profile-desc">{{topic.data.user.desc}}</div>
        </light-card>

        <light-card class="content-card">
          <div class="content-body" v-html="convertToHtml(topic.data.content)"></div>
          <el-divider/>
          <div class="content-meta">
            <div>Posted at: {{new Date(topic.data.time).toLocaleString()}}</div>
          </div>
          <div class="content-actions">
            <interact-button name="Edit Post" color="dodgerblue" :check="false"
                             @check="edit = true" v-if="store.user.id === topic.data.user.id">
              <el-icon><EditPen/></el-icon>
            </interact-button>
            <interact-button name="Give a Like" check-name="Liked" color="pink" :check="topic.like"
                             @check="interact('like', 'Like')">
              <el-icon><CircleCheck/></el-icon>
            </interact-button>
            <interact-button name="Bookmark" check-name="Bookmarked" color="orange" :check="topic.collect"
                             @check="interact('collect', 'Bookmark')">
              <el-icon><Star/></el-icon>
            </interact-button>
          </div>
        </light-card>
      </div>

      <transition name="el-fade-in-linear" mode="out-in">
        <div v-if="topic.comments" class="comments-section">
          <div class="section-heading">{{topic.data.comments}} Comments</div>
          <div class="comments-list">
            <light-card class="comment-card" v-for="item in topic.comments" :key="item.id">
              <div class="comment-header">
                <div class="comment-author">
                  <el-avatar :src="store.avatarUserUrl(item.user.avatar)" :size="48"/>
                  <div>
                    <div class="comment-name">
                      {{item.user.username}}
                      <span class="gender" v-if="item.user.gender === 1">
                        <el-icon><Female/></el-icon>
                      </span>
                      <span class="gender male" v-if="item.user.gender === 0">
                        <el-icon><Male/></el-icon>
                      </span>
                    </div>
                    <div class="comment-email">{{item.user.email}}</div>
                  </div>
                </div>
                <div class="comment-time">Commented at: {{new Date(item.time).toLocaleString()}}</div>
              </div>
              <div v-if="item.quote" class="comment-quote">Reply to: {{item.quote}}</div>
              <div class="comment-body" v-html="convertToHtml(item.content)"></div>
              <div class="comment-actions">
                <el-link :icon="ChatSquare" @click="comment.show = true;comment.quote = item" type="info">
                  Reply
                </el-link>
                <el-link :icon="Delete" type="danger" v-if="item.user.id === store.user.id"
                         @click="deleteComment(item.id)">
                  Delete
                </el-link>
              </div>
            </light-card>
          </div>
          <div class="comments-pagination">
            <el-pagination background layout="prev, pager, next"
                           v-model:current-page="topic.page" @current-change="loadComments"
                           :total="topic.data.comments" :page-size="10"
                           hide-on-single-page/>
          </div>
        </div>
      </transition>
    </div>
    <topic-editor :show="edit" @close="edit = false" v-if="topic.data && store.forum.types"
                  :default-type="topic.data.type" :default-text="topic.data.content"
                  :default-title="topic.data.title" submit-button="Update Post Content" :submit="updateTopic"/>
    <topic-comment-editor :show="comment.show" @close="comment.show = false" :tid="tid"
                          :quote="comment.quote" @comment="onCommentAdd"/>
    <div class="add-comment" @click="comment.show = true;comment.quote = null">
      <el-icon><Plus/></el-icon>
    </div>
  </div>
</template>

<style lang="less" scoped>
.topic-detail-page {
  width: 100%;
  display: flex;
  justify-content: center;
  background: radial-gradient(circle at top, rgba(255, 255, 255, 0.75), rgba(228, 233, 245, 0.65));
  padding: 36px 0 72px;
  position: relative;
}

.topic-detail-shell {
  width: min(980px, 100%);
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0 32px;
  box-sizing: border-box;
}

.topic-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-radius: 22px;
  backdrop-filter: blur(28px);
  background: rgba(255, 255, 255, 0.68);
}

.toolbar-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: rgba(15, 23, 42, 0.9);
}

.topic-main {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 20px;
}

.profile-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  border-radius: 26px;
  padding: 28px 24px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(26px);
}

.profile-header {
  display: flex;
  gap: 16px;
  align-items: center;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-name {
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.profile-email {
  font-size: 13px;
  color: rgba(71, 85, 105, 0.8);
}

.gender {
  color: hotpink;
  display: inline-flex;
  align-items: center;
}

.gender.male {
  color: dodgerblue;
}

.profile-divider {
  margin: 0;
}

.profile-contact {
  display: flex;
  flex-direction: column;
  gap: 10px;

  & > div {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    color: rgba(71, 85, 105, 0.85);
  }

  & span:first-child {
    font-weight: 600;
  }
}

.profile-desc {
  font-size: 13px;
  color: rgba(71, 85, 105, 0.85);
  line-height: 1.6;
}

.content-card {
  border-radius: 30px;
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 22px;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(28px);
}

.content-body {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(15, 23, 42, 0.86);
}

.content-meta {
  text-align: center;
  font-size: 13px;
  color: rgba(100, 116, 139, 0.85);
}

.content-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: flex-end;
}

.comments-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-top: 12px;
}

.section-heading {
  font-size: 16px;
  font-weight: 600;
  color: rgba(15, 23, 42, 0.85);
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-card {
  border-radius: 26px;
  padding: 24px 28px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(26px);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.comment-author {
  display: flex;
  gap: 14px;
  align-items: center;
}

.comment-name {
  font-weight: 600;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.comment-email {
  font-size: 13px;
  color: rgba(71, 85, 105, 0.8);
}

.comment-time {
  font-size: 12px;
  color: rgba(100, 116, 139, 0.85);
}

.comment-quote {
  font-size: 13px;
  color: rgba(71, 85, 105, 0.85);
  background: rgba(59, 130, 246, 0.08);
  padding: 12px 16px;
  border-radius: 16px;
  margin: 16px 0 0;
}

.comment-body {
  font-size: 14px;
  line-height: 1.7;
  color: rgba(15, 23, 42, 0.85);
  margin-top: 18px;
}

.comment-actions {
  margin-top: 20px;
  display: flex;
  gap: 18px;
  justify-content: flex-end;
}

.comments-pagination {
  display: flex;
  justify-content: center;
}

.add-comment {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  font-size: 20px;
  color: var(--el-color-primary);
  text-align: center;
  line-height: 54px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 38px rgba(15, 23, 42, 0.15);
  backdrop-filter: blur(22px);
  transition: all 0.3s ease;

  &:hover {
    background: rgba(241, 245, 249, 0.92);
    cursor: pointer;
    transform: translateY(-2px);
  }
}

.dark {
  .topic-detail-page {
    background: radial-gradient(circle at top, rgba(30, 30, 30, 0.88), rgba(8, 8, 8, 0.95));
  }

  .topic-toolbar,
  .profile-card,
  .content-card,
  .comment-card {
    background: rgba(26, 26, 26, 0.78);
    color: rgba(229, 231, 235, 0.88);
  }

  .toolbar-title,
  .profile-name,
  .section-heading,
  .comment-name,
  .comment-body,
  .content-body {
    color: rgba(229, 231, 235, 0.92);
  }

  .profile-email,
  .profile-contact span,
  .profile-desc,
  .comment-email,
  .comment-time,
  .comment-quote,
  .content-meta {
    color: rgba(148, 163, 184, 0.85);
  }

  .comment-quote {
    background: rgba(96, 165, 250, 0.18);
  }

  .add-comment {
    background: rgba(38, 38, 38, 0.9);
    color: rgba(165, 180, 252, 1);
  }
}

@media (max-width: 960px) {
  .topic-detail-shell {
    padding: 0 18px;
  }

  .topic-main {
    grid-template-columns: 1fr;
  }

  .add-comment {
    bottom: 20px;
    right: 20px;
  }
}
</style>
