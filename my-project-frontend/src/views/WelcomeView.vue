<script setup>
import bg from '@/assets/111.jpg'
</script>

<template>
  <div class="welcome-container">
    <section class="hero-panel">
      <div class="hero-background">
        <el-image class="hero-image" :src="bg" fit="cover"/>
      </div>
      <div class="hero-overlay"></div>
      <div class="hero-inner">
        <span class="hero-badge">StudySphere</span>
        <h1 class="hero-title">Learn boldly. Create beautifully.</h1>
        <p class="hero-description">
          Experience a refined campus community inspired by the elegance of Apple design.
          Plan your study sessions, stay current with events, and grow alongside creators who value craft.
        </p>
        <div class="hero-features">
          <span class="feature-chip">Immersive learning journeys</span>
          <span class="feature-chip">Curated campus events</span>
          <span class="feature-chip">Effortless collaboration</span>
        </div>
      </div>
    </section>

    <section class="auth-panel">
      <div class="auth-card">
        <router-view v-slot="{ Component }">
          <transition name="el-fade-in-linear" mode="out-in">
            <component :is="Component" class="auth-component"/>
          </transition>
        </router-view>
      </div>
      <div class="auth-subtext">Designed for clarity, focus, and a touch of wonder.</div>
    </section>
  </div>
</template>

<style scoped>
.welcome-container {
  width: 100%;
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 2.2fr) minmax(320px, 1fr);
  gap: 56px;
  padding: 64px 80px;
  box-sizing: border-box;
  position: relative;
}

.hero-panel {
  position: relative;
  border-radius: 42px;
  overflow: hidden;
  display: flex;
  align-items: stretch;
  background: linear-gradient(140deg, #0f172a, #1e293b 48%, #312e81);
  color: #ffffff;
  box-shadow: 0 40px 80px rgba(15, 23, 42, 0.35);
  isolation: isolate;
}

.hero-background,
.hero-overlay {
  position: absolute;
  inset: 0;
}

.hero-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: saturate(125%) contrast(110%);
  opacity: 0.35;
}

.hero-overlay {
  background: radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.35), transparent 55%),
              radial-gradient(circle at 80% 30%, rgba(148, 163, 255, 0.55), transparent 60%),
              linear-gradient(120deg, rgba(15, 23, 42, 0.65), rgba(49, 46, 129, 0.55));
  mix-blend-mode: lighten;
  opacity: 0.9;
}

.hero-inner {
  position: relative;
  padding: 80px 70px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 28px;
  max-width: 620px;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.22);
  color: rgba(255, 255, 255, 0.92);
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-size: 13px;
  backdrop-filter: blur(14px);
}

.hero-title {
  font-size: clamp(2.8rem, 3.6vw, 3.6rem);
  font-weight: 700;
  line-height: 1.05;
  margin: 0;
}

.hero-description {
  font-size: 1.05rem;
  line-height: 1.8;
  max-width: 520px;
  color: rgba(255, 255, 255, 0.82);
  margin: 0;
}

.hero-features {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.feature-chip {
  display: inline-flex;
  align-items: center;
  padding: 9px 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  color: rgba(255, 255, 255, 0.86);
  font-size: 0.85rem;
  letter-spacing: 0.02em;
  backdrop-filter: blur(16px);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.35);
}

.auth-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 20px;
  position: relative;
}

.auth-card {
  border-radius: 32px;
  padding: 40px 32px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.18);
  backdrop-filter: blur(28px);
  border: 1px solid rgba(255, 255, 255, 0.65);
}

.auth-component {
  height: 100%;
}

.auth-subtext {
  font-size: 0.9rem;
  color: rgba(15, 23, 42, 0.55);
  letter-spacing: 0.02em;
  text-align: center;
}

.dark .welcome-container {
  background: transparent;
}

.dark .hero-panel {
  background: linear-gradient(150deg, rgba(15, 23, 42, 0.92), rgba(30, 41, 59, 0.94));
  box-shadow: 0 40px 100px rgba(0, 0, 0, 0.65);
}

.dark .hero-overlay {
  opacity: 0.6;
}

.dark .feature-chip {
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.9);
  box-shadow: inset 0 0 0 1px rgba(148, 163, 255, 0.22);
}

.dark .auth-card {
  background: rgba(17, 24, 39, 0.86);
  border: 1px solid rgba(148, 163, 255, 0.22);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.6);
}

.dark .auth-subtext {
  color: rgba(226, 232, 240, 0.55);
}

@media (max-width: 1440px) {
  .welcome-container {
    padding: 48px 48px;
    gap: 40px;
  }

  .hero-inner {
    padding: 64px 56px;
  }
}

@media (max-width: 1100px) {
  .welcome-container {
    grid-template-columns: 1fr;
    padding: 48px 36px 120px;
  }

  .auth-panel {
    max-width: 460px;
    margin: -120px auto 0;
  }
}

@media (max-width: 720px) {
  .welcome-container {
    padding: 32px 24px 96px;
  }

  .hero-inner {
    padding: 48px 32px;
  }

  .auth-panel {
    margin-top: -96px;
  }

  .auth-card {
    padding: 32px 24px;
  }
}
</style>
