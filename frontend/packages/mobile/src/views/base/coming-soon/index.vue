<template>
  <div class="coming-soon">
    <div class="cs-card">
      <div class="cs-logo">FLOW</div>
      <h1 class="cs-title">敬请期待</h1>
      <p class="cs-sub">产品正在准备中，敬请期待正式上线</p>
    </div>
  </div>
</template>

<script lang="ts" setup>
  // 上线占位页：由 App.vue 顶部的 COMING_SOON 开关控制显隐，无需改动本文件。
</script>

<style lang="less" scoped>
  .coming-soon {
    position: fixed;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    // 品牌渐变（带 hex 兜底，防止全局变量未注入时透明）
    background: linear-gradient(160deg, var(--primary-8, #3a6df0) 0%, var(--primary-7, #2451c9) 100%);
    color: #fff;
  }

  .cs-card {
    text-align: center;
    padding: 0 32px;
  }

  .cs-logo {
    width: 88px;
    height: 88px;
    margin: 0 auto 28px;
    border-radius: 22px;
    background: rgba(255, 255, 255, 0.16);
    backdrop-filter: blur(6px);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
    font-weight: 700;
    letter-spacing: 1px;
  }

  .cs-title {
    font-size: 30px;
    font-weight: 700;
    margin: 0 0 12px;
    letter-spacing: 4px;
  }

  .cs-sub {
    font-size: 14px;
    line-height: 1.6;
    opacity: 0.85;
    margin: 0;
  }
</style>
