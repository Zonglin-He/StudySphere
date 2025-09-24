<script setup>
import {Back, Lock, Message, Operation, Right} from "@element-plus/icons-vue";
import {useStore} from "@/store";
import {isRoleAdmin, logout} from "@/net";
import router from "@/router";
import {computed} from "vue";
import {useRoute} from "vue-router";

const route = useRoute()
const store = useStore()

const isAdminPage = computed(() => route.fullPath.startsWith("/admin"))

function userLogout() {
  logout(() => router.push("/"))
}
</script>

<template>
  <div class="user-info">
    <template v-if="isRoleAdmin()">
      <el-button type="primary" size="small"
                 @click="router.push('/index')"
                 v-if="isAdminPage">
        Back to User Side
        <el-icon style="margin-left: 5px">
          <Right/>
        </el-icon>
      </el-button>
      <el-button type="danger" size="small"
                 @click="router.push('/admin')"
                 v-else>
        Go to Admin Side
        <el-icon style="margin-left: 5px">
          <Right/>
        </el-icon>
      </el-button>
    </template>
    <slot/>
    <div class="profile">
      <div>{{ store.user.username }}</div>
      <div>{{ store.user.email }}</div>
    </div>
    <el-dropdown>
      <el-avatar :src="store.avatarUrl"/>
      <template #dropdown>
        <el-dropdown-item @click="router.push('/index/user-setting')">
          <el-icon>
            <Operation/>
          </el-icon>
          Profile Settings
        </el-dropdown-item>
        <el-dropdown-item @click="router.push('/index/privacy-setting')">
          <el-icon>
            <Lock/>
          </el-icon>
          Account Security
        </el-dropdown-item>
        <el-dropdown-item @click="userLogout" divided>
          <el-icon>
            <Back/>
          </el-icon>
          Logout
        </el-dropdown-item>
      </template>
    </el-dropdown>
  </div>
</template>

<style scoped>
.user-info {
  width: 100%;
  display: flex;
  gap: 18px;
  justify-content: flex-end;
  align-items: center;
  color: inherit;

  .el-avatar {
    box-shadow: 0 12px 24px rgba(58, 78, 110, 0.22);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
  }

  .el-avatar:hover {
    cursor: pointer;
    transform: translateY(-2px);
    box-shadow: 0 18px 30px rgba(58, 78, 110, 0.28);
  }

  .profile {
    text-align: right;

    :first-child {
      font-size: 17px;
      font-weight: 600;
      line-height: 20px;
      letter-spacing: 0.3px;
    }

    :last-child {
      font-size: 11px;
      color: rgba(86, 96, 112, 0.85);
      letter-spacing: 0.6px;
      text-transform: uppercase;
    }
  }
}

.dark .user-info .profile :last-child {
  color: rgba(194, 203, 226, 0.85);
}
</style>
