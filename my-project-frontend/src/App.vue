<script setup>
import { useDark, useToggle } from '@vueuse/core'
import {onMounted, provide, ref} from "vue";
import {isUnauthorized} from "@/net";
import {apiUserInfo} from "@/net/api/user";
import {apiForumTypes} from "@/net/api/forum";
import {useStore} from "@/store";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import en from "element-plus/es/locale/lang/en";

useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: 'light'
})

useDark({
  onChanged(dark) { useToggle(dark) }
})

const loading = ref(false)
const store = useStore()
provide('userLoading', loading)

onMounted(() => {
    if(!isUnauthorized()) {
        apiUserInfo(loading)
    }
    // Only load forum types if backend is available
    if(!isUnauthorized()) {
        apiForumTypes(data => store.forum.types = data)
    }
})
</script>

<template>
  <el-config-provider :locale="en">
      <div class="wrapper">
          <router-view/>
      </div>
  </el-config-provider>
</template>

<style scoped>
.wrapper {
  line-height: 1.5;
}
</style>
