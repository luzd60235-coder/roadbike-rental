<template>
  <div class="page-shell" v-loading="loading">
    <section v-if="store.id" class="content-card store-hero">
      <div class="store-cover detail-cover">
        <img class="cover-image" :src="store.imageUrl" :alt="store.name" />
        <div class="cover-title">{{ store.name }}</div>
        <div class="cover-subtitle">{{ store.city }} · {{ store.district }}</div>
      </div>

      <div class="store-hero-body">
        <div>
          <h1 class="page-title">{{ store.name }}</h1>
          <p class="page-subtitle">{{ store.description || '支持当前门店车辆查看与在线预约。' }}</p>
        </div>

        <div class="store-actions">
          <el-button plain @click="router.push('/user/stores')">返回门店列表</el-button>
        </div>
      </div>

      <div class="section-grid cols-2">
        <div class="soft-card">
          <div class="info-pair"><span class="muted">门店地址</span><strong>{{ store.address }}</strong></div>
          <div class="info-pair"><span class="muted">联系电话</span><strong>{{ store.phone }}</strong></div>
          <div class="info-pair"><span class="muted">营业时间</span><strong>{{ store.hours }}</strong></div>
          <div class="info-pair"><span class="muted">当前库存</span><strong>{{ store.available }} / {{ store.capacity }}</strong></div>
        </div>

        <div class="soft-card">
          <h3 class="section-title">门店服务</h3>
          <el-space wrap>
            <el-tag v-for="service in store.services" :key="service" effect="plain">{{ service }}</el-tag>
          </el-space>
        </div>
      </div>
    </section>

    <section class="metric-grid" v-if="store.id">
      <article class="metric-card">
        <div class="metric-label">门店车辆</div>
        <div class="metric-value">{{ summary.totalBikes }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">可预约</div>
        <div class="metric-value">{{ summary.availableBikes }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">租赁中</div>
        <div class="metric-value">{{ summary.rentingBikes }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">维修中</div>
        <div class="metric-value">{{ summary.maintenanceBikes }}</div>
      </article>
    </section>

    <section class="toolbar-card" v-if="store.id">
      <div class="filters-row">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索品牌 / 车型 / 状态"
          style="width: 280px"
        />
        <el-select v-model="status" clearable placeholder="选择状态" style="width: 180px">
          <el-option
            v-for="item in statusOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </div>
      <div class="filter-summary">
        当前门店共展示 <strong>{{ filteredBikes.length }}</strong> 辆车，可直接对“可租赁”车辆发起预约。
      </div>
    </section>

    <section v-if="filteredBikes.length > 0" class="grid-cards">
      <div v-for="item in filteredBikes" :key="item.id" class="bike-list-card">
        <div class="bike-cover">
          <img class="cover-image" :src="item.coverImageUrl" :alt="item.bikeName" />
        </div>
        <div class="bike-card-body">
          <div class="bike-head">
            <div>
              <div class="bike-card-title">{{ item.bikeName }}</div>
              <div class="muted">{{ item.size }} / {{ item.color }}</div>
            </div>
            <el-tag :type="item.statusType">{{ item.status }}</el-tag>
          </div>

          <div class="bike-desc">{{ item.summary }}</div>

          <div class="info-pair"><span class="muted">变速配置</span><strong>{{ item.gear }}</strong></div>
          <div class="info-pair"><span class="muted">日租金</span><strong>￥ {{ item.price }}</strong></div>

          <div class="bike-card-footer">
            <el-button plain @click="router.push(`/user/bikes/${item.id}`)">查看详情</el-button>
            <el-button
              type="primary"
              :disabled="item.statusCode !== 1"
              @click="openBookingDialog(item)"
            >
              {{ item.statusCode === 1 ? '预约下单' : '当前不可预约' }}
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <section v-else-if="store.id" class="content-card">
      <el-empty description="当前筛选条件下暂无车辆" />
    </section>

    <el-dialog v-model="bookingVisible" title="预约下单" width="520px" destroy-on-close>
      <el-form ref="bookingFormRef" :model="bookingForm" :rules="bookingRules" label-position="top">
        <el-form-item label="预约车辆">
          <el-input :model-value="selectedBike?.bikeName || ''" disabled />
        </el-form-item>
        <el-form-item label="取车门店">
          <el-input :model-value="store.name || ''" disabled />
        </el-form-item>
        <el-form-item label="计划取车时间" prop="plannedStartTime">
          <el-date-picker
            v-model="bookingForm.plannedStartTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="请选择取车时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="计划还车时间" prop="plannedEndTime">
          <el-date-picker
            v-model="bookingForm.plannedEndTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="请选择还车时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="bookingForm.remark"
            type="textarea"
            :rows="3"
            maxlength="60"
            show-word-limit
            placeholder="可填写到店时间、骑行需求等"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="bookingVisible = false">取消</el-button>
        <el-button type="primary" :loading="bookingLoading" @click="handleCreateOrder">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createUserOrder, getUserStoreDetail } from '@/api/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const bookingLoading = ref(false)
const bookingVisible = ref(false)
const bookingFormRef = ref()
const store = ref({})
const bikes = ref([])
const selectedBike = ref(null)
const keyword = ref('')
const status = ref('')
const summary = ref({
  totalBikes: 0,
  availableBikes: 0,
  rentingBikes: 0,
  maintenanceBikes: 0
})

const bookingForm = reactive({
  pickupStoreId: null,
  plannedStartTime: '',
  plannedEndTime: '',
  remark: ''
})

const bookingRules = {
  plannedStartTime: [{ required: true, message: '请选择取车时间', trigger: 'change' }],
  plannedEndTime: [{ required: true, message: '请选择还车时间', trigger: 'change' }]
}

const statusOptions = computed(() =>
  [...new Set(bikes.value.map((item) => item.status).filter(Boolean))]
)

const filteredBikes = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()

  return bikes.value.filter((item) => {
    const matchKeyword =
      !normalizedKeyword ||
      item.bikeName.toLowerCase().includes(normalizedKeyword) ||
      item.brand.toLowerCase().includes(normalizedKeyword) ||
      item.model.toLowerCase().includes(normalizedKeyword) ||
      item.status.toLowerCase().includes(normalizedKeyword)
    const matchStatus = !status.value || item.status === status.value
    return matchKeyword && matchStatus
  })
})

function formatDateTimeValue(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

function buildDefaultBookingWindow() {
  const start = new Date()
  start.setMinutes(0, 0, 0)
  start.setHours(start.getHours() + 2)

  const end = new Date(start)
  end.setDate(end.getDate() + 1)

  return {
    plannedStartTime: formatDateTimeValue(start),
    plannedEndTime: formatDateTimeValue(end)
  }
}

function resetBookingForm() {
  const defaults = buildDefaultBookingWindow()
  bookingForm.pickupStoreId = store.value.id || null
  bookingForm.plannedStartTime = defaults.plannedStartTime
  bookingForm.plannedEndTime = defaults.plannedEndTime
  bookingForm.remark = ''
}

async function loadDetail(storeId) {
  loading.value = true
  try {
    const data = await getUserStoreDetail(storeId)
    store.value = data.store
    bikes.value = data.bikes
    summary.value = data.summary
    resetBookingForm()
  } finally {
    loading.value = false
  }
}

function openBookingDialog(bike) {
  if (!bike || bike.statusCode !== 1) {
    ElMessage.warning('当前车辆不可预约，请选择可租赁车辆。')
    return
  }

  selectedBike.value = bike
  resetBookingForm()
  bookingVisible.value = true
}

async function handleCreateOrder() {
  const valid = await bookingFormRef.value.validate().catch(() => false)
  if (!valid || !selectedBike.value) {
    return
  }

  if (bookingForm.plannedEndTime <= bookingForm.plannedStartTime) {
    ElMessage.warning('还车时间必须晚于取车时间')
    return
  }

  bookingLoading.value = true
  try {
    const order = await createUserOrder({
      bikeId: selectedBike.value.bikeId || selectedBike.value.id,
      pickupStoreId: bookingForm.pickupStoreId,
      plannedStartTime: bookingForm.plannedStartTime,
      plannedEndTime: bookingForm.plannedEndTime,
      remark: bookingForm.remark.trim()
    })

    bookingVisible.value = false
    ElMessage.success(`预约成功，订单号 ${order.orderNo}`)
    await loadDetail(store.value.id)
    router.push('/user/orders')
  } finally {
    bookingLoading.value = false
  }
}

watch(
  () => route.params.id,
  (id) => {
    if (id) {
      loadDetail(id)
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.store-hero {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-cover {
  min-height: 260px;
}

.store-hero-body {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.store-actions {
  display: flex;
  gap: 12px;
}

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

.bike-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}

.filter-summary {
  margin-top: 14px;
  color: var(--text-secondary);
  font-size: 14px;
}

@media (max-width: 900px) {
  .store-hero-body {
    flex-direction: column;
  }
}
</style>
