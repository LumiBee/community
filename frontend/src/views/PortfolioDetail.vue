<template>
  <div class="portfolio-detail-page">
    <div class="container-fluid" style="max-width: 1200px;">
      <h2>作品集详情</h2>
      <p class="text-muted">这里是作品集详情页面，功能待实现...</p>
      <router-link to="/portfolio" class="btn btn-warning">返回作品集</router-link>
    </div>
  </div>
</template>

<script setup>
const props = defineProps(['id'])
</script>

<style scoped>
.portfolio-detail-page {
  padding: 2rem 0;
}
</style>
