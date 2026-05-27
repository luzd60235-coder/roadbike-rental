<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">我的收藏</h1>
        <p class="page-subtitle">保存心仪车型，方便后续快速对比与再次下单。</p>
      </div>
      <el-button plain @click="router.push('/user/bikes')">继续浏览</el-button>
    </section>

    <section class="grid-cards">
      <div v-for="item in list" :key="item.id" class="bike-list-card">
        <div class="bike-cover">
          <img class="cover-image" :src="item.coverImageUrl" :alt="item.bikeName" />
        </div>
        <div class="bike-card-body">
          <div class="bike-card-title">{{ item.bikeName }}</div>
          <div class="muted" style="margin-top: 8px">收藏时间：{{ item.addedAt }}</div>
          <div class="muted" style="margin-top: 10px">{{ item.summary }}</div>
          <div class="bike-card-footer">
            <div class="price-highlight">¥ {{ item.price }}</div>
            <div class="favorite-actions">
              <el-button type="primary" @click="router.push(`/user/bikes/${item.id}`)">查看详情</el-button>
              <el-button
                plain
                type="danger"
                :loading="removingModelId === item.modelId"
                @click="handleRemove(item)"
              >
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserFavorites, removeUserFavorite } from '@/api/user'

const router = useRouter()
const list = ref([])
const removingModelId = ref(null)

async function loadFavorites() {
  const data = await getUserFavorites()
  list.value = data.list
}

async function handleRemove(item) {
  removingModelId.value = item.modelId
  try {
    await removeUserFavorite(item.modelId)
    ElMessage.success('已取消收藏')
    await loadFavorites()
  } finally {
    removingModelId.value = null
  }
}

onMounted(loadFavorites)
</script>

<style scoped>
.bike-list-card {
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

.bike-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
}

.favorite-actions {
  display: flex;
  gap: 10px;
}
</style>
