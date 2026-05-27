<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">门店查询</h1>
        <p class="page-subtitle">按城市查找门店，点击后可查看当前门店车辆并直接预约下单。</p>
      </div>
      <el-button plain @click="router.push('/user/bikes')">查看全部车辆</el-button>
    </section>

    <section class="toolbar-card">
      <div class="filters-row">
        <el-input v-model="keyword" placeholder="搜索门店名 / 区域" clearable style="width: 280px" />
        <el-select v-model="city" clearable placeholder="城市" style="width: 160px">
          <el-option label="上海" value="上海" />
          <el-option label="苏州" value="苏州" />
          <el-option label="杭州" value="杭州" />
        </el-select>
      </div>
    </section>

    <section class="grid-cards">
      <div v-for="store in filteredList" :key="store.id" class="store-query-card">
        <div class="store-cover">
          <img class="cover-image" :src="store.imageUrl" :alt="store.name" />
          <div class="cover-title">{{ store.name }}</div>
          <div class="cover-subtitle">{{ store.city }} · {{ store.district }}</div>
        </div>
        <div class="bike-card-body">
          <div class="info-pair"><span class="muted">门店地址</span><strong>{{ store.address }}</strong></div>
          <div class="info-pair"><span class="muted">联系电话</span><strong>{{ store.phone }}</strong></div>
          <div class="info-pair"><span class="muted">营业时间</span><strong>{{ store.hours }}</strong></div>
          <div class="info-pair"><span class="muted">可用库存</span><strong>{{ store.available }} / {{ store.capacity }}</strong></div>
          <el-space wrap style="margin-top: 16px">
            <el-tag v-for="service in store.services" :key="service" effect="plain">{{ service }}</el-tag>
          </el-space>
          <div class="store-actions">
            <el-button type="primary" @click="router.push(`/user/stores/${store.id}`)">查看当前门店车辆</el-button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserStores } from '@/api/user'

const router = useRouter()
const list = ref([])
const keyword = ref('')
const city = ref('')

const filteredList = computed(() =>
  list.value.filter((item) => {
    const matchKeyword =
      !keyword.value ||
      item.name.toLowerCase().includes(keyword.value.toLowerCase()) ||
      item.district.toLowerCase().includes(keyword.value.toLowerCase())
    const matchCity = !city.value || item.city === city.value
    return matchKeyword && matchCity
  })
)

onMounted(async () => {
  const data = await getUserStores()
  list.value = data.list
})
</script>

<style scoped>
.store-query-card {
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
}

.bike-card-body {
  padding: 18px;
}

.store-actions {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}
</style>
