<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">订单管理</h1>
        <p class="page-subtitle">查看订单状态并支持后台补充备注、取消异常订单。</p>
      </div>
      <el-button type="primary" @click="loadData">刷新订单</el-button>
    </section>

    <section class="metric-grid">
      <article class="metric-card" v-for="item in metrics" :key="item.label">
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}</div>
        <div class="metric-trend">{{ item.tip }}</div>
      </article>
    </section>

    <section class="toolbar-card">
      <div class="filters-row">
        <el-input v-model="keyword" placeholder="搜索订单号 / 用户 / 车型" clearable style="width: 320px" />
        <el-select v-model="status" placeholder="订单状态" clearable style="width: 180px">
          <el-option label="待取车" value="待取车" />
          <el-option label="租赁中" value="租赁中" />
          <el-option label="待结算" value="待结算" />
          <el-option label="已完成" value="已完成" />
          <el-option label="已取消" value="已取消" />
        </el-select>
      </div>
    </section>

    <section class="content-card">
      <el-table :data="filteredList" v-loading="loading" stripe>
        <el-table-column label="订单号" prop="orderNo" min-width="150" />
        <el-table-column label="用户 / 车型" min-width="220">
          <template #default="{ row }">
            <div>{{ row.userName }}</div>
            <div class="muted">{{ row.bikeName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="取还门店" min-width="220">
          <template #default="{ row }">
            <div>取：{{ row.pickupStore }}</div>
            <div class="muted">还：{{ row.returnStore }}</div>
          </template>
        </el-table-column>
        <el-table-column label="租期" min-width="180">
          <template #default="{ row }">
            <div>{{ row.startTime }}</div>
            <div class="muted">{{ row.endTime }}</div>
          </template>
        </el-table-column>
        <el-table-column label="租金" min-width="120">
          <template #default="{ row }">¥ {{ row.rentAmount }}</template>
        </el-table-column>
        <el-table-column label="押金" min-width="100">
          <template #default="{ row }">¥ {{ row.deposit }}</template>
        </el-table-column>
        <el-table-column label="支付状态" prop="paymentStatus" min-width="120" />
        <el-table-column label="订单状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.statusType">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" title="处理订单" width="560px" destroy-on-close>
      <template v-if="currentOrder">
        <div class="info-pair"><span class="muted">订单号</span><strong>{{ currentOrder.orderNo }}</strong></div>
        <div class="info-pair"><span class="muted">用户</span><strong>{{ currentOrder.userName }}</strong></div>
        <div class="info-pair"><span class="muted">车型</span><strong>{{ currentOrder.bikeName }}</strong></div>
        <div class="info-pair"><span class="muted">当前状态</span><strong>{{ currentOrder.status }}</strong></div>

        <el-form label-position="top" style="margin-top: 16px">
          <el-form-item label="后台备注">
            <el-input
              v-model="remark"
              type="textarea"
              :rows="3"
              maxlength="120"
              show-word-limit
              placeholder="记录订单处理说明、人工跟进情况等"
            />
          </el-form-item>
          <el-form-item label="取消原因">
            <el-input
              v-model="cancelReason"
              :disabled="!canCancel(currentOrder)"
              placeholder="仅在取消订单时使用"
            />
          </el-form-item>
        </el-form>
      </template>

      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveRemark">保存备注</el-button>
        <el-button
          type="danger"
          plain
          :disabled="!canCancel(currentOrder)"
          :loading="saving"
          @click="handleCancelOrder"
        >
          取消订单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminOrders, updateAdminOrder } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const keyword = ref('')
const status = ref('')
const list = ref([])
const metrics = ref([])
const currentOrder = ref(null)
const remark = ref('')
const cancelReason = ref('')

const filteredList = computed(() =>
  list.value.filter((item) => {
    const normalizedKeyword = keyword.value.trim().toLowerCase()
    const matchKeyword =
      !normalizedKeyword ||
      item.orderNo.toLowerCase().includes(normalizedKeyword) ||
      item.userName.toLowerCase().includes(normalizedKeyword) ||
      item.bikeName.toLowerCase().includes(normalizedKeyword)
    const matchStatus = !status.value || item.status === status.value
    return matchKeyword && matchStatus
  })
)

function canCancel(order) {
  if (!order) {
    return false
  }
  return ![5, 6].includes(order.statusCode)
}

async function loadData() {
  loading.value = true
  try {
    const data = await getAdminOrders()
    list.value = data.list
    metrics.value = [
      { label: '今日新单', value: data.summary.todayOrders, tip: '近 24 小时新增订单' },
      { label: '待取车', value: data.summary.pendingPickup, tip: '等待门店完成交付' },
      { label: '租赁中', value: data.summary.runningOrders, tip: '关注还车与异常' },
      { label: '已完成', value: data.summary.completedOrders, tip: '适合复盘门店履约' }
    ]
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  currentOrder.value = row
  remark.value = row.remark || ''
  cancelReason.value = row.cancelReason || ''
  dialogVisible.value = true
}

async function handleSaveRemark() {
  if (!currentOrder.value) {
    return
  }

  saving.value = true
  try {
    await updateAdminOrder(currentOrder.value.orderId, {
      remark: remark.value.trim()
    })
    ElMessage.success('订单备注已保存')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleCancelOrder() {
  if (!currentOrder.value || !canCancel(currentOrder.value)) {
    return
  }

  try {
    await ElMessageBox.confirm(`确认取消订单 ${currentOrder.value.orderNo} 吗？`, '取消订单', {
      type: 'warning'
    })
  } catch {
    return
  }

  saving.value = true
  try {
    await updateAdminOrder(currentOrder.value.orderId, {
      orderStatus: 6,
      cancelReason: cancelReason.value.trim() || '后台取消订单',
      remark: remark.value.trim()
    })
    ElMessage.success('订单已取消')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>
