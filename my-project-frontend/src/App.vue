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
      <div class="wrapper apple-wrapper">
          <router-view/>
      </div>
  </el-config-provider>
</template>

<style scoped>
.wrapper {
  line-height: 1.5;
  min-height: 100vh;
}

.apple-wrapper {
  display: flex;
  flex-direction: column;
  background: linear-gradient(145deg, rgba(239, 244, 255, 0.9), rgba(255, 255, 255, 0.8));
}

.dark .apple-wrapper {
  background: radial-gradient(circle at top, rgba(32, 35, 48, 0.95), rgba(14, 16, 24, 0.98));
}
</style>

<style>
:root {
  font-family: "SF Pro Display", "SF Pro Text", -apple-system, BlinkMacSystemFont, "Helvetica Neue", "Segoe UI", sans-serif;
  color: #0a0a0a;
  background-color: #f5f6f8;
  font-smooth: always;
  -webkit-font-smoothing: antialiased;
}

body {
  margin: 0;
  background-color: transparent;
}

html.dark {
  color: rgba(255, 255, 255, 0.9);
  background-color: #0b0c12;
}
</style>
