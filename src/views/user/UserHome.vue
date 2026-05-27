<template>
  <div class="page-shell">
    <section class="page-header hero-gradient">
      <div class="hero-content">
        <el-tag type="success" effect="dark">热门骑行服务</el-tag>
        <h1 class="page-title" style="color: #f8fafc; margin-top: 18px">{{ hero.title }}</h1>
        <p class="page-subtitle" style="color: rgba(248, 250, 252, 0.82)">{{ hero.subtitle }}</p>
      </div>

      <div class="hero-actions">
        <el-button type="primary" @click="goToBikes">立即选车</el-button>
        <el-button @click="goToStores">查询门店</el-button>
      </div>
    </section>

    <section class="metric-grid">
      <article v-for="item in quickStats" :key="item.label" class="metric-card">
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}</div>
      </article>
    </section>

    <section class="content-card">
      <h3 class="section-title">推荐车型</h3>
      <div class="grid-cards">
        <div v-for="item in recommended" :key="item.id" class="bike-card">
          <div class="bike-cover">
            <img class="cover-image" :src="item.coverImageUrl" :alt="item.bikeName" />
          </div>
          <div class="bike-card-body">
            <div class="bike-card-title">{{ item.bikeName }}</div>
            <div class="muted">{{ item.summary }}</div>
            <div class="bike-tags">
              <el-tag v-for="tag in item.tags.slice(0, 2)" :key="tag" effect="plain">{{ tag }}</el-tag>
            </div>
            <div class="bike-card-footer">
              <div class="price-highlight">￥ {{ item.price }}</div>
              <el-button type="primary" link @click="router.push(`/user/bikes/${item.id}`)">查看详情</el-button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="content-card">
      <h3 class="section-title">热门门店</h3>
      <div class="grid-cards">
        <div v-for="store in stores" :key="store.id" class="store-card">
          <div class="store-cover">
            <img class="cover-image" :src="store.imageUrl" :alt="store.name" />
            <div class="cover-title">{{ store.name }}</div>
            <div class="cover-subtitle">{{ store.city }} / {{ store.district }}</div>
          </div>
          <div class="bike-card-body">
            <div class="info-pair">
              <span class="muted">营业时间</span>
              <strong>{{ store.hours }}</strong>
            </div>
            <div class="info-pair">
              <span class="muted">当前可用</span>
              <strong>{{ store.available }} / {{ store.capacity }}</strong>
            </div>
            <el-space wrap style="margin-top: 14px">
              <el-tag v-for="service in store.services" :key="service" effect="plain">{{ service }}</el-tag>
            </el-space>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserHome } from '@/api/user'

const router = useRouter()
const hero = ref({
  title: '',
  subtitle: ''
})
const quickStats = ref([])
const recommended = ref([])
const stores = ref([])

function goToBikes() {
  router.push('/user/bikes')
}

function goToStores() {
  router.push('/user/stores')
}

onMounted(async () => {
  const data = await getUserHome()
  hero.value = data.hero
  quickStats.value = data.quickStats
  recommended.value = data.recommended
  stores.value = data.stores
})
</script>

<style scoped>
.hero-actions {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  min-width: 280px;
  flex-wrap: wrap;
}

.bike-card,
.store-card {
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
}

.bike-card-body {
  padding: 18px;
}

.bike-card-title {
  font-size: 18px;
  font-weight: 700;
}

.bike-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.bike-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
}
</style>
