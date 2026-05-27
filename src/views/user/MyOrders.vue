<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">我的订单</h1>
        <p class="page-subtitle">查看订单履约状态、金额明细和历史骑行记录。</p>
      </div>
      <el-button type="primary" @click="router.push('/user/bikes')">继续租车</el-button>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">累计订单</div>
        <div class="metric-value">{{ list.length }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">进行中</div>
        <div class="metric-value">{{ runningCount }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">历史消费</div>
        <div class="metric-value">¥ {{ totalCost }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">常用门店</div>
        <div class="metric-value" style="font-size: 20px">{{ commonStore }}</div>
      </article>
    </section>

    <section class="content-card">
      <div v-for="item in list" :key="item.orderNo" class="order-card">
        <div>
          <div class="order-title">
            <strong>{{ item.bikeName }}</strong>
            <el-tag :type="item.statusType">{{ item.status }}</el-tag>
          </div>
          <div class="muted">{{ item.orderNo }} · {{ item.startTime }} 至 {{ item.endTime }}</div>
          <div class="muted" style="margin-top: 8px">取车门店：{{ item.pickupStore }} / 还车门店：{{ item.returnStore }}</div>
        </div>
        <div class="order-side">
          <div class="price-highlight">¥ {{ item.rentAmount }}</div>
          <div class="muted">押金 ¥ {{ item.deposit }}</div>
          <el-button
            v-if="canCancel(item)"
            link
            type="danger"
            :loading="cancelingOrderId === item.orderId"
            @click="handleCancel(item)"
          >
            取消订单
          </el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelUserOrder, getUserOrders } from '@/api/user'

const router = useRouter()
const list = ref([])
const cancelingOrderId = ref(null)

const runningCount = computed(() => list.value.filter((item) => ['待取车', '租赁中', '待结算'].includes(item.status)).length)
const totalCost = computed(() => list.value.reduce((total, item) => total + item.rentAmount, 0))
const commonStore = computed(() => {
  const counts = new Map()

  list.value.forEach((item) => {
    const storeName = item.pickupStore
    if (storeName) {
      counts.set(storeName, (counts.get(storeName) || 0) + 1)
    }
  })

  return [...counts.entries()].sort((left, right) => right[1] - left[1])[0]?.[0] || '暂无'
})

function canCancel(item) {
  return [1, 2].includes(item.statusCode) || ['待支付', '待取车'].includes(item.status)
}

async function loadOrders() {
  const data = await getUserOrders()
  list.value = data.list
}

async function handleCancel(item) {
  try {
    await ElMessageBox.confirm(`确认取消订单 ${item.orderNo} 吗？`, '取消订单', {
      type: 'warning'
    })
  } catch {
    return
  }

  cancelingOrderId.value = item.orderId
  try {
    await cancelUserOrder(item.orderId)
    ElMessage.success('订单已取消')
    await loadOrders()
  } finally {
    cancelingOrderId.value = null
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.order-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 0;
  border-bottom: 1px dashed rgba(15, 23, 42, 0.08);
}

.order-card:last-child {
  border-bottom: 0;
}

.order-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.order-side {
  min-width: 140px;
  text-align: right;
}
</style>
