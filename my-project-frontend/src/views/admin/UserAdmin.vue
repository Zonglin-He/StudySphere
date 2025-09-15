<script setup>
import {EditPen, User} from "@element-plus/icons-vue";
import {apiUserDetailTotal, apiUserList, apiUserSave} from "@/net/api/user";
import {reactive, watchEffect} from "vue";
import {useStore} from "@/store";
import {ElMessage} from "element-plus";

const store = useStore()

const editor = reactive({
  id: 0,
  display: false,
  temp: {},
  loading: false
})

const userTable = reactive({
  page: 1,
  size: 10,
  total: 0,
  data: []
})

function userStatus(user) {
  if(user.mute && user.banned)
    return 'Muted, Banned'
  else if(user.mute)
    return 'Muted'
  else if(user.banned)
    return 'Banned'
  else
    return 'Active'
}

function openUserEditor(user) {
  editor.id = user.id
  editor.display = true
  editor.loading = true
  apiUserDetailTotal(editor.id, data => {
    editor.temp = { ...data, ...user }
    editor.loading = false
  })
}

function saveUserDetail() {
  editor.display = false
  apiUserSave(editor.temp, () => {
    const user = userTable.data.find(user => user.id === editor.id)
    Object.assign(user, editor.temp)
    ElMessage.success('Data saved successfully')
  })
}

watchEffect(() => apiUserList(userTable.page, userTable.size, data => {
  userTable.total = data.total
  userTable.data = data.list
}))
</script>

<template>
  <div class="user-admin">
    <div class="title">
      <el-icon><User/></el-icon>
      Forum User List
    </div>
    <div class="desc">
      Manage all forum users here, including account information, bans, and mutes.
    </div>
    <el-table :data="userTable.data" height="320">
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column label="Username" width="180">
        <template #default="{ row }">
          <div class="table-username">
            <el-avatar :size="30" :src="store.avatarUserUrl(row.avatar)"/>
            <div>{{ row.username }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Role" width="100" align="center">
        <template #default="{ row }">
          <el-tag type="danger" v-if="row.role === 'admin'">Admin</el-tag>
          <el-tag v-else>User</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="Email"/>
      <el-table-column label="Registration Time">
        <template #default="{ row }">
          {{ new Date(row.registerTime).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column label="Status" align="center">
        <template #default="{ row }">
          {{ userStatus(row) }}
        </template>
      </el-table-column>
      <el-table-column label="Actions" align="center">
        <template #default="{ row }">
          <el-button type="primary" size="small" :icon="EditPen"
                     @click="openUserEditor(row)"
                     :disabled="row.role === 'admin'">Edit</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination :total="userTable.total"
                     v-model:current-page="userTable.page"
                     v-model:page-size="userTable.size"
                     layout="total, sizes, prev, pager, next, jumper"/>
    </div>
    <el-drawer v-model="editor.display">
      <template #header>
        <div>
          <div style="font-weight: bold">
            <el-icon><EditPen/></el-icon> Edit User Information
          </div>
          <div style="font-size: 13px">After editing, please click the save button below</div>
        </div>
      </template>
      <el-form label-position="top">
        <el-form-item label="Username">
          <el-input v-model="editor.temp.username"/>
        </el-form-item>
        <el-form-item label="Email">
          <el-input v-model="editor.temp.email"/>
        </el-form-item>
        <div style="display: flex;font-size: 14px;gap: 20px">
          <div>
            <span style="margin-right: 10px">Mute</span>
            <el-switch v-model="editor.temp.mute"/>
          </div>
          <el-divider style="height: 30px" direction="vertical"/>
          <div>
            <span style="margin-right: 10px">Ban Account</span>
            <el-switch v-model="editor.temp.banned"/>
          </div>
        </div>
        <div style="margin-top: 10px;color: #606266;font-size: 14px">
          Registration Time: {{ new Date(editor.temp.registerTime).toLocaleString() }}
        </div>
        <el-divider/>
      </el-form>
      <template #footer>
        <div style="text-align: center">
          <el-button type="success" @click="saveUserDetail">Save</el-button>
          <el-button type="info" @click="editor.display = false">Cancel</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style lang="less" scoped>
.user-admin {
  .title {
    font-weight: bold;
  }

  .desc {
    color: #bababa;
    font-size: 13px;
    margin-bottom: 20px;
  }

  .table-username {
    height: 30px;
    display: flex;
    align-items: center;
    gap: 15px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: right;
  }

  :deep(.el-drawer__header) {
    margin-bottom: 0;
  }
}
</style>
