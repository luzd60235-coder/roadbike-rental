<template>
  <div class="page-shell" v-loading="loading">
    <section class="section-grid cols-2" v-if="bike.id">
      <div class="content-card">
        <div class="bike-cover detail-cover">
          <img class="cover-image" :src="bike.coverImageUrl" :alt="bike.bikeName" />
        </div>

        <div style="margin-top: 20px">
          <h2 style="margin: 0; font-size: 30px">{{ bike.bikeName }}</h2>
          <p class="page-subtitle">{{ bike.summary }}</p>
        </div>

        <div class="detail-specs">
          <div class="info-pair"><span class="muted">车架材质</span><strong>{{ bike.frame }}</strong></div>
          <div class="info-pair"><span class="muted">变速系统</span><strong>{{ bike.gear }}</strong></div>
          <div class="info-pair"><span class="muted">制动系统</span><strong>{{ bike.brake }}</strong></div>
          <div class="info-pair"><span class="muted">轮组规格</span><strong>{{ bike.wheel }}</strong></div>
          <div class="info-pair"><span class="muted">车身重量</span><strong>{{ bike.weight }}</strong></div>
        </div>
      </div>

      <div class="soft-card">
        <el-tag :type="bike.statusType">{{ bike.status }}</el-tag>

        <div class="price-block">
          <div class="price-highlight">￥ {{ bike.price }} / 天</div>
          <div v-if="bike.hasMaintenanceDiscount" class="discount-note">
            <span class="line-through">原价 ￥{{ bike.originalPrice }} / 天</span>
            <span>该车已有 {{ bike.maintenanceHistoryCount }} 次维修记录，当前已优惠 {{ bike.maintenanceDiscountPercent }}%</span>
          </div>
          <div v-else class="muted" style="margin-top: 8px">暂无维修历史，当前为标准租金。</div>
          <div class="muted" style="margin-top: 8px">押金 ￥{{ bike.deposit }}，支持异店还车与租前试骑</div>
        </div>

        <div class="detail-specs" style="margin-top: 22px">
          <div class="info-pair"><span class="muted">所在门店</span><strong>{{ bike.storeName }}</strong></div>
          <div class="info-pair"><span class="muted">所在城市</span><strong>{{ bike.city }}</strong></div>
          <div class="info-pair"><span class="muted">尺码 / 颜色</span><strong>{{ bike.size }} / {{ bike.color }}</strong></div>
          <div class="info-pair"><span class="muted">累计里程</span><strong>{{ bike.mileage }} km</strong></div>
        </div>

        <el-space wrap style="margin-top: 16px">
          <el-tag v-for="tag in bike.tags" :key="tag" effect="plain">{{ tag }}</el-tag>
          <el-tag v-if="bike.maintenanceHistoryCount > 0" type="warning" effect="plain">
            维修历史 {{ bike.maintenanceHistoryCount }} 次
          </el-tag>
        </el-space>

        <div class="detail-actions">
          <el-button
            type="primary"
            size="large"
            :disabled="bookingDisabled"
            :loading="bookingLoading"
            @click="openBookingDialog"
          >
            {{ bookingDisabled ? '当前不可预约' : '立即预约' }}
          </el-button>
          <el-button size="large" :loading="favoriteLoading" @click="handleToggleFavorite">
            {{ isFavorite ? '取消收藏' : '加入收藏' }}
          </el-button>
        </div>

        <div v-if="bookingDisabled" class="muted" style="margin-top: 10px">
          当前车辆状态为“{{ bike.status }}”，暂时不能直接预约。
        </div>
      </div>
    </section>

    <section class="section-grid cols-2" v-if="bike.id">
      <div class="content-card">
        <h3 class="section-title">维修历史</h3>
        <el-empty v-if="maintenanceHistory.length === 0" description="该车暂无维修历史" />
        <div v-else>
          <div v-for="item in maintenanceHistory" :key="item.maintenanceId" class="maintenance-row">
            <div class="maintenance-row-head">
              <div>
                <strong>{{ item.maintenanceType || '常规维修' }}</strong>
                <div class="muted">{{ item.storeName || '门店未记录' }}</div>
              </div>
              <el-tag :type="item.statusType">{{ item.status }}</el-tag>
            </div>
            <div class="maintenance-grid">
              <div class="info-pair"><span class="muted">故障描述</span><strong>{{ item.issue || '-' }}</strong></div>
              <div class="info-pair"><span class="muted">维修费用</span><strong>￥ {{ item.cost }}</strong></div>
              <div class="info-pair"><span class="muted">开始时间</span><strong>{{ item.startTime || item.reportTime || '-' }}</strong></div>
              <div class="info-pair"><span class="muted">结束时间</span><strong>{{ item.endTime || '-' }}</strong></div>
            </div>
            <div v-if="item.remark" class="muted maintenance-remark">备注：{{ item.remark }}</div>
          </div>
        </div>
      </div>

      <div class="content-card">
        <h3 class="section-title">同城可取门店</h3>
        <div v-for="store in nearbyStores" :key="store.id" class="nearby-store">
          <div>
            <strong>{{ store.name }}</strong>
            <div class="muted">{{ store.address }}</div>
          </div>
          <div class="muted">{{ store.hours }}</div>
        </div>
      </div>
    </section>

    <section class="content-card" v-if="bike.id">
      <h3 class="section-title">相似推荐</h3>
      <div v-if="similar.length === 0" class="muted">暂无更多同类车辆</div>
      <div v-else>
        <div v-for="item in similar" :key="item.id" class="recommend-row">
          <div>
            <strong>{{ item.bikeName }}</strong>
            <div class="muted">
              {{ item.type }} · ￥ {{ item.price }}/天
              <span v-if="item.hasMaintenanceDiscount"> · 维修历史 {{ item.maintenanceHistoryCount }} 次</span>
            </div>
          </div>
          <el-button link type="primary" @click="router.push(`/user/bikes/${item.id}`)">查看</el-button>
        </div>
      </div>
    </section>

    <el-dialog v-model="bookingVisible" title="提交预约" width="520px" destroy-on-close>
      <el-form ref="bookingFormRef" :model="bookingForm" :rules="bookingRules" label-position="top">
        <el-form-item label="预约车辆">
          <el-input :model-value="bike.bikeName" disabled />
        </el-form-item>
        <el-form-item label="取车门店">
          <el-input :model-value="bike.storeName" disabled />
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
            placeholder="可填写骑行需求、到店时间说明等"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="bookingVisible = false">取消</el-button>
        <el-button type="primary" :loading="bookingLoading" @click="handleCreateOrder">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  addUserFavorite,
  createUserOrder,
  getBikeDetail,
  getUserFavoriteModelIds,
  removeUserFavorite
} from '@/api/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const favoriteLoading = ref(false)
const bookingLoading = ref(false)
const bookingVisible = ref(false)
const bookingFormRef = ref()
const bike = ref({})
const maintenanceHistory = ref([])
const nearbyStores = ref([])
const similar = ref([])
const isFavorite = ref(false)

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

const bookingDisabled = computed(() => bike.value.statusCode !== 1)

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

function resetBookingForm(currentBike = bike.value) {
  const defaults = buildDefaultBookingWindow()
  bookingForm.pickupStoreId = currentBike.storeId || null
  bookingForm.plannedStartTime = defaults.plannedStartTime
  bookingForm.plannedEndTime = defaults.plannedEndTime
  bookingForm.remark = ''
}

async function loadDetail(id) {
  loading.value = true
  try {
    const [data, favoriteModelIds] = await Promise.all([getBikeDetail(id), getUserFavoriteModelIds()])
    bike.value = data.bike
    maintenanceHistory.value = data.maintenanceHistory
    nearbyStores.value = data.nearbyStores
    similar.value = data.similar
    isFavorite.value = favoriteModelIds.includes(data.bike.modelId)
    resetBookingForm(data.bike)
  } finally {
    loading.value = false
  }
}

function openBookingDialog() {
  if (bookingDisabled.value) {
    ElMessage.warning('当前车辆状态不可预约，请选择其他可租赁车辆。')
    return
  }

  resetBookingForm()
  bookingVisible.value = true
}

async function handleToggleFavorite() {
  if (!bike.value.modelId) {
    return
  }

  favoriteLoading.value = true
  try {
    if (isFavorite.value) {
      await removeUserFavorite(bike.value.modelId)
      isFavorite.value = false
      ElMessage.success('已取消收藏')
      return
    }

    await addUserFavorite(bike.value.modelId)
    isFavorite.value = true
    ElMessage.success('已加入收藏')
  } finally {
    favoriteLoading.value = false
  }
}

async function handleCreateOrder() {
  const valid = await bookingFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  if (bookingForm.plannedEndTime <= bookingForm.plannedStartTime) {
    ElMessage.warning('还车时间必须晚于取车时间')
    return
  }

  bookingLoading.value = true
  try {
    const order = await createUserOrder({
      bikeId: bike.value.bikeId || bike.value.id,
      pickupStoreId: bookingForm.pickupStoreId,
      plannedStartTime: bookingForm.plannedStartTime,
      plannedEndTime: bookingForm.plannedEndTime,
      remark: bookingForm.remark.trim()
    })

    bookingVisible.value = false
    bike.value = {
      ...bike.value,
      status: '已预订',
      statusType: 'warning',
      statusCode: 2
    }
    ElMessage.success(`预约成功，订单号 ${order.orderNo}`)
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
.detail-cover {
  min-height: 280px;
}

.detail-specs {
  margin-top: 18px;
}

.price-block {
  margin-top: 18px;
}

.discount-note {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 8px;
  color: var(--text-secondary);
}

.line-through {
  text-decoration: line-through;
}

.detail-actions {
  display: flex;
  gap: 12px;
  margin-top: 22px;
}

.maintenance-row {
  padding: 16px 0;
  border-bottom: 1px dashed rgba(15, 23, 42, 0.08);
}

.maintenance-row:last-child {
  border-bottom: 0;
}

.maintenance-row-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.maintenance-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 16px;
  margin-top: 12px;
}

.maintenance-remark {
  margin-top: 10px;
}

.nearby-store,
.recommend-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px dashed rgba(15, 23, 42, 0.08);
}

.nearby-store:last-child,
.recommend-row:last-child {
  border-bottom: 0;
}

@media (max-width: 900px) {
  .maintenance-grid {
    grid-template-columns: 1fr;
  }
}
</style>
