<script setup>
import bg from '@/assets/111.jpg' // 本地图片由 Vite 解析成可用的 URL
</script>

<template>
  <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">

    <div style="flex: 1; background-color: black; display: flex; justify-content: center; align-items: center;">
      <el-image
          style="height: 100%; width: auto"
          :src="bg"
      />
    </div>
    <div class="welcome-title">
      <div style="font-size: 30px;font-weight: bold">Welcome to our learning platform</div>
      <div style="margin-top: 10px">Here you can learn how to use Java, how to build websites, and communicate closely with the father of Java.</div>
      <div style="margin-top: 5px">Here you can make gay friends, because it’s all men—no girls study Java.</div>
    </div>
    <div class="right-card">
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component" style="height: 100%"/>
        </transition>
      </router-view>
    </div>
  </div>
</template>

<style scoped>
.right-card {
  width: 400px;
  z-index: 1;
  background-color: var(--el-bg-color);
}
.welcome-title {
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}
</style>
