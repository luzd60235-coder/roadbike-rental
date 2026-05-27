<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">结算管理</h1>
        <p class="page-subtitle">查看门店结算记录，并对已还车订单发起结算。</p>
      </div>
      <el-button plain @click="openCreateDialog">发起结算</el-button>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">本期应收</div>
        <div class="metric-value">￥ {{ overview.currentIncome }}</div>
        <div class="metric-trend">按结算记录自动汇总</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">退款金额</div>
        <div class="metric-value">￥ {{ overview.refundAmount }}</div>
        <div class="metric-trend">含押金及异常退款</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">待复核单据</div>
        <div class="metric-value">{{ overview.pendingReview }}</div>
        <div class="metric-trend">建议优先核对异常订单</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">维修成本</div>
        <div class="metric-value">￥ {{ overview.maintenanceCost }}</div>
        <div class="metric-trend">关联维修工单自动核销</div>
      </article>
    </section>

    <section class="content-card">
      <h3 class="section-title">结算清单</h3>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="订单号" prop="orderNo" min-width="150" />
        <el-table-column label="结算时间" prop="cycle" min-width="180" />
        <el-table-column label="门店" prop="storeName" min-width="140" />
        <el-table-column label="订单数" prop="orderCount" min-width="90" />
        <el-table-column label="租金收入" min-width="120">
          <template #default="{ row }">￥ {{ row.rentIncome }}</template>
        </el-table-column>
        <el-table-column label="退款" min-width="110">
          <template #default="{ row }">￥ {{ row.refundAmount }}</template>
        </el-table-column>
        <el-table-column label="维修成本" min-width="120">
          <template #default="{ row }">￥ {{ row.maintenanceCost }}</template>
        </el-table-column>
        <el-table-column label="到账金额" min-width="120">
          <template #default="{ row }">￥ {{ row.finalIncome }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="row.statusType">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" title="发起结算" width="760px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="待结算订单" prop="orderId">
          <el-select
            v-model="form.orderId"
            placeholder="选择待结算订单"
            filterable
            style="width: 100%"
            @change="handleOrderChange"
          >
            <el-option
              v-for="item in settlementOrderOptions"
              :key="item.orderId"
              :label="`${item.orderNo} / ${item.customer}`"
              :value="item.orderId"
            >
              <div class="option-title">{{ item.orderNo }}</div>
              <div class="option-subtitle">
                {{ item.customer }} / {{ item.bikeName }} / 押金 ￥ {{ item.deposit }}
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <div v-if="selectedOrder" class="soft-card settlement-hint">
          <div class="info-pair"><span class="muted">车辆</span><strong>{{ selectedOrder.bikeName }}</strong></div>
          <div class="info-pair"><span class="muted">订单金额</span><strong>￥ {{ selectedOrder.amount }}</strong></div>
          <div class="info-pair"><span class="muted">已付押金</span><strong>￥ {{ selectedOrder.deposit }}</strong></div>
          <div class="info-pair"><span class="muted">基础租金</span><strong>￥ {{ form.baseRentAmount }}</strong></div>
          <div class="info-pair"><span class="muted">超时费用</span><strong>￥ {{ form.overtimeAmount }}</strong></div>
          <div class="settlement-tip">
            {{ overtimeTip }}
          </div>
        </div>

        <div class="section-grid cols-2">
          <el-form-item label="基础租金" prop="baseRentAmount">
            <el-input-number v-model="form.baseRentAmount" :min="0" :precision="2" style="width: 100%" disabled />
          </el-form-item>
          <el-form-item label="超时费用" prop="overtimeAmount">
            <el-input-number v-model="form.overtimeAmount" :min="0" :precision="2" style="width: 100%" disabled />
          </el-form-item>
          <el-form-item label="维修赔偿" prop="maintenanceCompensation">
            <el-input-number v-model="form.maintenanceCompensation" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="遗失赔偿" prop="lossCompensation">
            <el-input-number v-model="form.lossCompensation" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="优惠金额" prop="discountAmount">
            <el-input-number v-model="form.discountAmount" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
        </div>

        <el-form-item label="结算备注">
          <el-input
            v-model="form.settlementNote"
            type="textarea"
            :rows="3"
            maxlength="120"
            show-word-limit
            placeholder="可填写押金扣减说明、现场验车情况等"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确认结算</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createStaffSettlement, getStaffSettlements, getStaffTrades } from '@/api/staff'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const list = ref([])
const orderOptions = ref([])
const overview = ref({
  currentIncome: 0,
  refundAmount: 0,
  pendingReview: 0,
  maintenanceCost: 0
})

const form = reactive({
  orderId: null,
  baseRentAmount: 0,
  overtimeAmount: 0,
  maintenanceCompensation: 0,
  lossCompensation: 0,
  discountAmount: 0,
  settlementNote: ''
})

const rules = {
  orderId: [{ required: true, message: '请选择待结算订单', trigger: 'change' }],
  baseRentAmount: [{ required: true, message: '请输入基础租金', trigger: 'change' }],
  overtimeAmount: [{ required: true, message: '请输入超时费用', trigger: 'change' }],
  maintenanceCompensation: [{ required: true, message: '请输入维修赔偿', trigger: 'change' }],
  lossCompensation: [{ required: true, message: '请输入遗失赔偿', trigger: 'change' }],
  discountAmount: [{ required: true, message: '请输入优惠金额', trigger: 'change' }]
}

const settlementOrderOptions = computed(() =>
  orderOptions.value.filter((item) => item.statusCode === 4)
)

const selectedOrder = computed(() =>
  settlementOrderOptions.value.find((item) => item.orderId === form.orderId) || null
)
const overtimeHours = computed(() => calculateOvertimeHours(selectedOrder.value))
const overtimeTip = computed(() => {
  if (!selectedOrder.value) {
    return ''
  }

  if (overtimeHours.value <= 0) {
    return '系统已按订单时间自动计算，本单按期归还，超时费用为 0。'
  }

  return `系统已按订单时间自动计算，本单超时 ${overtimeHours.value} 小时，按 ￥ ${selectedOrder.value.overtimeFeePerHour} / 小时计费。`
})

function resetForm() {
  Object.assign(form, {
    orderId: null,
    baseRentAmount: 0,
    overtimeAmount: 0,
    maintenanceCompensation: 0,
    lossCompensation: 0,
    discountAmount: 0,
    settlementNote: ''
  })
}

function parseDateTime(value) {
  if (!value) {
    return null
  }

  const normalized = String(value).replace(' ', 'T')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

function calculateOvertimeHours(order) {
  if (!order) {
    return 0
  }

  const plannedEndTime = parseDateTime(order.endTime)
  const actualReturnTime = parseDateTime(order.returnTime)
  if (!plannedEndTime || !actualReturnTime) {
    return 0
  }

  const overtimeMilliseconds = actualReturnTime.getTime() - plannedEndTime.getTime()
  if (overtimeMilliseconds <= 0) {
    return 0
  }

  return Math.ceil(overtimeMilliseconds / (60 * 60 * 1000))
}

function applyAutoAmounts(order) {
  if (!order) {
    form.baseRentAmount = 0
    form.overtimeAmount = 0
    return
  }

  form.baseRentAmount = Number(order.rentAmount || Math.max(0, Number(order.amount || 0) - Number(order.deposit || 0)))
  form.overtimeAmount = Number((calculateOvertimeHours(order) * Number(order.overtimeFeePerHour || 0)).toFixed(2))
}

function handleOrderChange(orderId) {
  const order = settlementOrderOptions.value.find((item) => item.orderId === orderId)
  if (!order) {
    return
  }

  applyAutoAmounts(order)
  form.maintenanceCompensation = 0
  form.lossCompensation = 0
  form.discountAmount = 0
}

async function loadSettlements() {
  loading.value = true
  try {
    const data = await getStaffSettlements()
    list.value = data.list
    overview.value = data.overview
  } finally {
    loading.value = false
  }
}

async function loadOrderOptions() {
  const data = await getStaffTrades()
  orderOptions.value = data.list
}

async function openCreateDialog() {
  resetForm()
  await loadOrderOptions()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    await createStaffSettlement({
      orderId: form.orderId,
      baseRentAmount: form.baseRentAmount,
      overtimeAmount: form.overtimeAmount,
      maintenanceCompensation: form.maintenanceCompensation,
      lossCompensation: form.lossCompensation,
      discountAmount: form.discountAmount,
      settlementNote: form.settlementNote.trim()
    })
    ElMessage.success('结算单已创建')
    dialogVisible.value = false
    await Promise.all([loadSettlements(), loadOrderOptions()])
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadSettlements(), loadOrderOptions()])
})
</script>

<style scoped>
.settlement-hint {
  margin-bottom: 20px;
}

.option-title {
  font-weight: 700;
}

.option-subtitle {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 12px;
}
</style>
