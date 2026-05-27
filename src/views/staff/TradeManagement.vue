<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">交易管理</h1>
        <p class="page-subtitle">处理取还车、订单核验和门店当班交易流水，支持跨门店办理租赁中车辆还车。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新建线下订单</el-button>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">待取车</div>
        <div class="metric-value">{{ summary.pendingPickup }}</div>
        <div class="metric-trend">优先准备车辆与护具</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">待还车</div>
        <div class="metric-value">{{ summary.waitingReturn }}</div>
        <div class="metric-trend">任意门店均可办理还车</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">今日租金</div>
        <div class="metric-value">¥ {{ summary.todayTurnover }}</div>
        <div class="metric-trend">今日实际收取的租金</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">当前班次</div>
        <div class="metric-value" style="font-size: 20px">{{ summary.currentShift }}</div>
        <div class="metric-trend">注意高峰时段车辆交接</div>
      </article>
    </section>

    <section class="toolbar-card">
      <div class="filters-row">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索订单号 / 客户 / 车型 / 车辆编号"
          style="width: 320px"
          @keyup.enter="loadData"
        />
        <el-select v-model="status" clearable placeholder="按状态筛选" style="width: 180px">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button plain @click="resetFilters">重置</el-button>
      </div>
      <div class="filter-summary">
        当前展示 <strong>{{ list.length }}</strong> 条订单记录，可按订单号、客户名、车型或车辆编号快速定位。
      </div>
    </section>

    <section class="content-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="订单号" prop="orderNo" min-width="150" />
        <el-table-column label="客户 / 车型" min-width="220">
          <template #default="{ row }">
            <div>{{ row.customer }}</div>
            <div class="muted">{{ row.bikeName }} · {{ row.bikeCode }}</div>
          </template>
        </el-table-column>
        <el-table-column label="取还时间" min-width="200">
          <template #default="{ row }">
            <div>取：{{ row.pickupTime }}</div>
            <div class="muted">还：{{ row.returnTime }}</div>
          </template>
        </el-table-column>
        <el-table-column label="门店流转" min-width="220">
          <template #default="{ row }">
            <div>取：{{ row.pickupStore }}</div>
            <div class="muted">还：{{ row.returnStore }}</div>
          </template>
        </el-table-column>
        <el-table-column label="金额" min-width="140">
          <template #default="{ row }">
            <div>租金: ¥ {{ row.rentAmount }}</div>
            <div class="muted">押金: ¥ {{ row.deposit }}</div>
          </template>
        </el-table-column>
        <el-table-column label="经办人" prop="operator" min-width="100" />
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.statusType">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.statusCode === 2"
              link
              type="primary"
              @click="handlePickup(row)"
            >
              办理取车
            </el-button>
            <el-button
              v-if="row.statusCode === 3"
              link
              type="primary"
              @click="handleReturn(row)"
            >
              办理还车
            </el-button>
            <span v-if="![2, 3].includes(row.statusCode)" class="muted">无需操作</span>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" title="新建线下订单" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" v-loading="optionsLoading">
        <div class="section-grid cols-2">
          <el-form-item label="下单用户" prop="userId">
            <el-select
              v-model="form.userId"
              placeholder="选择用户"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="item in userOptions"
                :key="item.value"
                :label="`${item.label} (${item.username})`"
                :value="item.value"
              >
                <div class="option-title">{{ item.label }}</div>
                <div class="option-subtitle">{{ item.username }}{{ item.phone ? ` / ${item.phone}` : '' }}</div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="预约车辆" prop="bikeId">
            <el-select
              v-model="form.bikeId"
              placeholder="选择当前门店可租车辆"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="item in bikeOptions"
                :key="item.value"
                :label="`${item.bikeCode} / ${item.bikeName}`"
                :value="item.value"
              >
                <div class="option-title">{{ item.bikeCode }}</div>
                <div class="option-subtitle">
                  {{ item.bikeName }}{{ item.frameSize ? ` / ${item.frameSize}` : '' }}{{ item.color ? ` / ${item.color}` : '' }}
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="计划取车时间" prop="plannedStartTime">
            <el-date-picker
              v-model="form.plannedStartTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择取车时间"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="计划还车时间" prop="plannedEndTime">
            <el-date-picker
              v-model="form.plannedEndTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择还车时间"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <el-form-item label="订单备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="120"
            show-word-limit
            placeholder="可填写线下接待说明、证件核验情况等"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确认建单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createStaffOfflineOrder,
  getStaffTradeFormOptions,
  getStaffTrades,
  pickupStaffOrder,
  returnStaffOrder
} from '@/api/staff'

const loading = ref(false)
const saving = ref(false)
const optionsLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const list = ref([])
const userOptions = ref([])
const bikeOptions = ref([])
const keyword = ref('')
const status = ref(null)
const summary = ref({
  pendingPickup: 0,
  waitingReturn: 0,
  todayTurnover: 0,
  currentShift: '-'
})
const statusOptions = [
  { label: '待支付', value: 1 },
  { label: '待取车', value: 2 },
  { label: '租赁中', value: 3 },
  { label: '待结算', value: 4 },
  { label: '已完成', value: 5 },
  { label: '已取消', value: 6 }
]

const form = reactive({
  userId: null,
  bikeId: null,
  plannedStartTime: '',
  plannedEndTime: '',
  remark: ''
})

const rules = {
  userId: [{ required: true, message: '请选择下单用户', trigger: 'change' }],
  bikeId: [{ required: true, message: '请选择预约车辆', trigger: 'change' }],
  plannedStartTime: [{ required: true, message: '请选择计划取车时间', trigger: 'change' }],
  plannedEndTime: [{ required: true, message: '请选择计划还车时间', trigger: 'change' }]
}

function normalizeDateTime(value) {
  return value ? value.replace(' ', 'T') : null
}

function resetForm() {
  Object.assign(form, {
    userId: null,
    bikeId: null,
    plannedStartTime: '',
    plannedEndTime: '',
    remark: ''
  })
}

async function loadData() {
  loading.value = true
  try {
    const data = await getStaffTrades({
      keyword: keyword.value,
      status: status.value
    })
    list.value = data.list
    summary.value = data.summary
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  keyword.value = ''
  status.value = null
  loadData()
}

async function loadFormOptions() {
  optionsLoading.value = true
  try {
    const data = await getStaffTradeFormOptions()
    userOptions.value = data.users || []
    bikeOptions.value = data.bikes || []
  } finally {
    optionsLoading.value = false
  }
}

async function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
  await loadFormOptions()
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    await createStaffOfflineOrder({
      userId: form.userId,
      bikeId: form.bikeId,
      plannedStartTime: normalizeDateTime(form.plannedStartTime),
      plannedEndTime: normalizeDateTime(form.plannedEndTime),
      remark: form.remark.trim()
    })
    ElMessage.success('线下订单已创建')
    dialogVisible.value = false
    await Promise.all([loadData(), loadFormOptions()])
  } finally {
    saving.value = false
  }
}

async function handlePickup(row) {
  try {
    await ElMessageBox.confirm(`确认将订单 ${row.orderNo} 办理为“租赁中”吗？`, '办理取车', {
      type: 'warning'
    })
  } catch {
    return
  }

  await pickupStaffOrder(row.orderId)
  ElMessage.success('订单状态已更新为租赁中')
  await loadData()
}

async function handleReturn(row) {
  try {
    await ElMessageBox.confirm(`确认已归还车辆，并将订单 ${row.orderNo} 进入待结算吗？`, '办理还车', {
      type: 'warning'
    })
  } catch {
    return
  }

  await returnStaffOrder(row.orderId)
  ElMessage.success('订单状态已更新为待结算')
  await loadData()
}

onMounted(async () => {
  await Promise.all([loadData(), loadFormOptions()])
})
</script>

<style scoped>
.option-title {
  font-weight: 700;
}

.option-subtitle {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 12px;
}

.filter-summary {
  margin-top: 14px;
  color: var(--text-secondary);
  font-size: 14px;
}
</style>
