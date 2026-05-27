<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">数据统计</h1>
        <p class="page-subtitle">从营收、热门车型、门店库存利用率和渠道占比观察整体经营表现。</p>
      </div>
      <el-button plain @click="downloadReport">下载报表</el-button>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">累计营收</div>
        <div class="metric-value">¥ {{ overview.revenue.toLocaleString() }}</div>
        <div class="metric-trend">较上月增长 16%</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">累计订单</div>
        <div class="metric-value">{{ overview.orders }}</div>
        <div class="metric-trend">本周峰值集中在周末上午</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">在营车辆</div>
        <div class="metric-value">{{ overview.activeBikes }}</div>
        <div class="metric-trend">可结合门店热度灵活调度库存</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">满意度</div>
        <div class="metric-value">{{ overview.satisfaction }}</div>
        <div class="metric-trend">骑后评价维持高位</div>
      </article>
    </section>

    <section class="section-grid cols-2">
      <div class="content-card">
        <h3 class="section-title">门店营收与库存利用率</h3>
        <div v-for="item in storePerformance" :key="item.name" class="store-row">
          <div>
            <strong>{{ item.name }}</strong>
            <div class="muted">
              订单 {{ item.orders }} 单 / 营收 ¥ {{ item.revenue.toLocaleString() }}
            </div>
          </div>
          <div class="store-progress">
            <el-progress :percentage="item.utilization" :stroke-width="10" />
            <div class="muted progress-meta">
              当前库存 {{ item.inventoryCount }} / 容量 {{ item.capacity }} · 可租 {{ item.available }}
            </div>
          </div>
        </div>
      </div>

      <div class="soft-card">
        <h3 class="section-title">渠道占比</h3>
        <div v-for="item in channels" :key="item.name" class="info-pair">
          <span class="muted">{{ item.name }}</span>
          <strong>{{ item.percent }}%</strong>
        </div>
      </div>
    </section>

    <section class="section-grid cols-2">
      <div class="content-card">
        <h3 class="section-title">营收趋势</h3>
        <div class="mini-bars">
          <div v-for="item in incomeTrend" :key="item.week" class="mini-bar">
            <div class="mini-bar-fill" :style="{ height: `${item.amount * 2.4}px` }"></div>
            <strong>{{ item.amount }}k</strong>
            <span class="mini-bar-label">{{ item.week }}</span>
          </div>
        </div>
      </div>

      <div class="content-card">
        <h3 class="section-title">热门车型</h3>
        <el-table :data="hotModels" stripe>
          <el-table-column label="车型" prop="model" min-width="220" />
          <el-table-column label="订单量" prop="orders" min-width="100" />
          <el-table-column label="占比" prop="growth" min-width="100" />
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminStatistics } from '@/api/admin'

const overview = ref({
  revenue: 0,
  orders: 0,
  activeBikes: 0,
  stores: 0,
  satisfaction: '0%'
})
const storePerformance = ref([])
const hotModels = ref([])
const incomeTrend = ref([])
const channels = ref([])

function escapeCsv(value) {
  const text = value == null ? '' : String(value)
  return `"${text.replace(/"/g, '""')}"`
}

function buildCsvRows() {
  const now = new Date()
  const dateLabel = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(
    now.getDate()
  ).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(
    now.getMinutes()
  ).padStart(2, '0')}`

  return [
    ['管理员统计报表'],
    ['生成时间', dateLabel],
    [],
    ['概览指标'],
    ['指标', '值'],
    ['累计营收', overview.value.revenue],
    ['累计订单', overview.value.orders],
    ['在营车辆', overview.value.activeBikes],
    ['门店数量', overview.value.stores],
    ['满意度', overview.value.satisfaction],
    [],
    ['门店营收与库存利用率'],
    ['门店', '订单量', '营收', '库存利用率', '当前库存', '门店容量', '可租车辆'],
    ...storePerformance.value.map((item) => [
      item.name,
      item.orders,
      item.revenue,
      `${item.utilization}%`,
      item.inventoryCount,
      item.capacity,
      item.available
    ]),
    [],
    ['渠道占比'],
    ['渠道', '占比'],
    ...channels.value.map((item) => [item.name, `${item.percent}%`]),
    [],
    ['营收趋势'],
    ['日期', '营收（千元）'],
    ...incomeTrend.value.map((item) => [item.week, item.amount]),
    [],
    ['热门车型'],
    ['车型', '订单量', '占比'],
    ...hotModels.value.map((item) => [item.model, item.orders, item.growth])
  ]
}

function downloadReport() {
  const rows = buildCsvRows()
  const csv = `\uFEFF${rows.map((row) => row.map(escapeCsv).join(',')).join('\r\n')}`
  const now = new Date()
  const fileName = `管理员统计报表-${now.getFullYear()}${String(now.getMonth() + 1).padStart(
    2,
    '0'
  )}${String(now.getDate()).padStart(2, '0')}.csv`
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')

  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('统计报表已开始下载')
}

onMounted(async () => {
  const data = await getAdminStatistics()
  overview.value = data.overview
  storePerformance.value = data.storePerformance
  hotModels.value = data.hotModels
  incomeTrend.value = data.incomeTrend
  channels.value = data.channels
})
</script>

<style scoped>
.store-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(220px, 0.9fr);
  align-items: center;
  gap: 18px;
  padding: 16px 0;
  border-bottom: 1px dashed rgba(15, 23, 42, 0.08);
}

.store-row:last-child {
  border-bottom: 0;
}

.store-progress {
  min-width: 0;
}

.progress-meta {
  margin-top: 8px;
  text-align: right;
}
</style>
