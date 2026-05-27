<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">车辆列表</h1>
        <p class="page-subtitle">按城市、门店、品牌和租赁状态筛选车辆，并查看维修历史与折后租金。</p>
      </div>
      <el-button plain @click="resetFilters">重置筛选</el-button>
    </section>

    <section class="toolbar-card">
      <div class="filters-row">
        <el-input
          v-model="keyword"
          placeholder="搜索品牌 / 车型 / 门店"
          clearable
          style="width: 280px"
        />
        <el-select v-model="city" clearable placeholder="选择城市" style="width: 160px">
          <el-option v-for="item in cityOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="status" clearable placeholder="选择状态" style="width: 160px">
          <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </div>

      <div class="filter-summary">
        当前共展示 <strong>{{ filteredList.length }}</strong> 辆车，
        其中有维修历史的车辆已自动按维修次数下调日租金。
      </div>
    </section>

    <section class="grid-cards">
      <div v-for="item in filteredList" :key="item.id" class="bike-list-card">
        <div class="bike-cover">
          <img class="cover-image" :src="item.coverImageUrl" :alt="item.bikeName" />
        </div>
        <div class="bike-card-body">
          <div class="bike-head">
            <div>
              <div class="bike-card-title">{{ item.bikeName }}</div>
              <div class="muted">{{ item.city }} · {{ item.storeName }}</div>
            </div>
            <el-tag :type="item.statusType">{{ item.status }}</el-tag>
          </div>

          <div class="bike-desc">{{ item.summary }}</div>

          <div class="specs-grid">
            <div class="spec-item" v-if="item.type">
              <span class="spec-label">类型</span>
              <span class="spec-value">{{ item.type }}</span>
            </div>
            <div class="spec-item" v-if="item.frame && item.frame !== '-'">
              <span class="spec-label">车架</span>
              <span class="spec-value">{{ item.frame }}</span>
            </div>
            <div class="spec-item" v-if="item.gear && item.gear !== '-'">
              <span class="spec-label">变速</span>
              <span class="spec-value">{{ item.gear }}</span>
            </div>
            <div class="spec-item" v-if="item.brake && item.brake !== '-'">
              <span class="spec-label">制动</span>
              <span class="spec-value">{{ item.brake }}</span>
            </div>
            <div class="spec-item" v-if="item.wheel && item.wheel !== '-'">
              <span class="spec-label">轮组</span>
              <span class="spec-value">{{ item.wheel }}</span>
            </div>
            <div class="spec-item" v-if="item.weight && item.weight !== '-'">
              <span class="spec-label">重量</span>
              <span class="spec-value">{{ item.weight }}</span>
            </div>
          </div>

          <div class="bike-card-footer">
            <div>
              <div class="price-highlight">￥ {{ item.price }}</div>
              <div v-if="item.hasMaintenanceDiscount" class="price-note">
                <span class="line-through">原价 ￥{{ item.originalPrice }}</span>
                <span>维修历史优惠 {{ item.maintenanceDiscountPercent }}%</span>
              </div>
            </div>
            <el-button type="primary" @click="router.push(`/user/bikes/${item.id}`)">查看详情</el-button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserBikes } from '@/api/user'

const router = useRouter()
const list = ref([])
const keyword = ref('')
const city = ref('')
const status = ref('')

const cityOptions = computed(() =>
  [...new Set(list.value.map((item) => item.city).filter(Boolean))].sort((a, b) =>
    a.localeCompare(b, 'zh-CN')
  )
)

const statusOptions = computed(() => [...new Set(list.value.map((item) => item.status).filter(Boolean))])

const filteredList = computed(() =>
  list.value.filter((item) => {
    const normalizedKeyword = keyword.value.trim().toLowerCase()
    const matchKeyword =
      !normalizedKeyword ||
      item.bikeName.toLowerCase().includes(normalizedKeyword) ||
      item.brand.toLowerCase().includes(normalizedKeyword) ||
      item.storeName.toLowerCase().includes(normalizedKeyword)
    const matchCity = !city.value || item.city === city.value
    const matchStatus = !status.value || item.status === status.value
    return matchKeyword && matchCity && matchStatus
  })
)

function resetFilters() {
  keyword.value = ''
  city.value = ''
  status.value = ''
}

onMounted(async () => {
  const data = await getUserBikes()
  list.value = data.list
})
</script>

<style scoped>
.bike-list-card {
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.06);
}

.bike-card-body {
  padding: 18px;
}

.bike-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.bike-card-title {
  font-size: 18px;
  font-weight: 700;
}

.bike-desc {
  min-height: 48px;
  margin: 14px 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.specs-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 12px;
}

.spec-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 70px;
  padding: 6px 10px;
  background: white;
  border-radius: 8px;
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.spec-label {
  font-size: 11px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.spec-value {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.bike-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}

.price-note {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 6px;
  color: var(--text-secondary);
  font-size: 13px;
}

.line-through {
  text-decoration: line-through;
}

.filter-summary {
  margin-top: 14px;
  color: var(--text-secondary);
  font-size: 14px;
}
</style>
