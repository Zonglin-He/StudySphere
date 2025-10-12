<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Object, default: () => ({}) }
})

const toText = (v, suffix = '') => (v ?? v === 0 ? `${v}${suffix}` : '—')
const hourLabel = (iso) => {
  if (!iso) return '— h'
  const t = new Date(iso)
  return isNaN(t.getTime()) ? '— h' : `${t.getHours()} h`
}

const view = computed(() => {
  const d = props.data || {}
  const now = d.now || {}
  const loc = d.location || {}

  // 顶行：国家 + 省份（缺就显示 —）
  const countryAdm =
      [loc.country, loc.adm1].filter(Boolean).join(' ') || '—'

  // 副行：优先 adm2，其次 name（但 name 为 “Current Location” 时就直接显示它，不再拼 District）
  let sub = loc.adm2 || loc.name || 'Current Location'
  if (sub && sub.toLowerCase() === 'current location') {
    // 保留原文，不附加任何后缀
  }

  return {
    success: !!d.success,
    icon: now.icon ?? (Array.isArray(d.hourly) && d.hourly[0]?.icon) ?? '100',
    temp: toText(now.temp, '°C'),
    text: now.text ?? '—',
    countryAdm,
    subline: sub,
    hourly: Array.isArray(d.hourly) ? d.hourly.slice(0, 5) : []
  }
})
</script>

<template>
  <div class="w-root" v-loading="!view.success" element-loading-text="Loading weather information...">
    <div v-if="view.success" class="w-header">
      <div class="w-icon"><i :class="`qi-${view.icon}-fill`"></i></div>

      <div class="w-main">
        <div class="w-temp">{{ view.temp }}</div>
        <div class="w-text">{{ view.text }}</div>
      </div>

      <div class="w-loc">
        <div class="w-loc-top">{{ view.countryAdm }}</div>
        <div class="w-loc-sub">{{ view.subline }}</div>
      </div>
    </div>

    <el-divider class="w-divider" />

    <div class="w-hourly">
      <div v-for="(item, idx) in view.hourly" :key="idx" class="w-hour-item">
        <div class="w-hour">{{ hourLabel(item.fxTime) }}</div>
        <div class="w-hour-icon"><i :class="`qi-${(item.icon ?? '100')}-fill`"></i></div>
        <div class="w-hour-temp">{{ toText(item.temp, '°C') }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.w-root{
  height: 180px;
  display: flex;
  flex-direction: column;
}

.w-header{
  display: grid;
  grid-template-columns: 64px 1fr minmax(140px, 40%);
  align-items: center;
  gap: 16px;
  padding: 8px 6px 2px;
}


.w-icon i{ font-size: 44px; color: rgba(100,116,139,.7); }

.w-main{
  min-width: 0;
}
.w-temp{ font-size: 32px; font-weight: 800; line-height: 1; }
.w-text{ font-size: 16px; margin-top: 6px; }

.w-loc{
  min-width: 0;           /* 允许收缩，否则会按内容把网格撑爆 */
  max-width: 100%;
  justify-self: end;      /* 靠右对齐 */
  text-align: right;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis; /* 超出显示省略号 */
}
.w-loc-top{ font-size: 15px; }
.w-loc-sub{ font-size: 14px; color: rgba(100,116,139,.9); }

.w-divider{ margin: 8px 0 10px !important; }

.w-hourly{
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  text-align: center;
}
.w-hour{ font-size: 13px; margin-bottom: 4px; }
.w-hour-icon i{ font-size: 22px; }
.w-hour-temp{ font-size: 12px; margin-top: 2px; }
</style>
