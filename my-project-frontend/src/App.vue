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
    <div class="app-layout">
      <div class="ambient-gradient" aria-hidden="true"></div>
      <div class="content-shell">
        <router-view />
      </div>
    </div>
  </el-config-provider>
</template>

<style lang="less">
:global(body) {
  margin: 0;
  font-family: 'SF Pro Display', 'SF Pro Text', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background-color: #f5f6f8;
  color: var(--el-text-color-primary);
}

.app-layout {
  position: relative;
  min-height: 100vh;
  line-height: 1.5;
  overflow: hidden;
}

.content-shell {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.ambient-gradient {
  position: fixed;
  inset: 0;
  z-index: -2;
  background:
    radial-gradient(45% 45% at 15% 20%, rgba(255, 192, 203, 0.6), transparent),
    radial-gradient(40% 40% at 80% 15%, rgba(173, 216, 255, 0.65), transparent),
    radial-gradient(35% 35% at 50% 75%, rgba(174, 255, 203, 0.55), transparent),
    linear-gradient(180deg, #f7f8fc 0%, #f1f2f6 100%);
  transition: background 0.3s ease;
}

.dark .ambient-gradient {
  background:
    radial-gradient(35% 35% at 15% 20%, rgba(80, 120, 255, 0.35), transparent),
    radial-gradient(30% 30% at 80% 15%, rgba(255, 120, 180, 0.25), transparent),
    radial-gradient(28% 28% at 50% 80%, rgba(120, 220, 255, 0.25), transparent),
    linear-gradient(180deg, #12131a 0%, #1a1b24 100%);
}

.dark :global(body) {
  background-color: #101117;
  color: #f3f4f6;
}
</style>
