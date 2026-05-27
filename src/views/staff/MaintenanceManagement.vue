<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">维修管理</h1>
        <p class="page-subtitle">记录车辆故障、派工状态和维修成本，帮助门店快速恢复可租库存。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增维修工单</el-button>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">待处理工单</div>
        <div class="metric-value">{{ overview.pendingJobs }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">维修中</div>
        <div class="metric-value">{{ overview.inProgress }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">今日完成</div>
        <div class="metric-value">{{ overview.completedToday }}</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">备件库存</div>
        <div class="metric-value" style="font-size: 22px">{{ overview.sparePartsStock }}</div>
      </article>
    </section>

    <section class="section-grid cols-2">
      <div class="content-card">
        <h3 class="section-title">维修工单</h3>
        <el-table :data="list" v-loading="loading" stripe>
          <el-table-column label="车辆编码" prop="bikeCode" min-width="130" />
          <el-table-column label="车型" prop="bikeName" min-width="180" />
          <el-table-column label="门店" prop="storeName" min-width="140" />
          <el-table-column label="故障描述" prop="issue" min-width="220" />
          <el-table-column label="等级" prop="level" min-width="90" />
          <el-table-column label="技师" prop="engineer" min-width="90" />
          <el-table-column label="费用" min-width="90">
            <template #default="{ row }">¥ {{ row.cost }}</template>
          </el-table-column>
          <el-table-column label="上报时间" prop="reportTime" min-width="160" />
          <el-table-column label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="row.statusType">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="180" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.statusCode === 1"
                link
                type="primary"
                @click="handleStart(row)"
              >
                开始维修
              </el-button>
              <el-button
                v-if="row.statusCode === 2"
                link
                type="success"
                @click="handleComplete(row)"
              >
                标记完成
              </el-button>
              <span v-if="row.statusCode === 3" class="muted">已完成</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="soft-card">
        <h3 class="section-title">待优先跟进</h3>
        <div v-for="item in urgentList" :key="item.id" class="urgent-item">
          <strong>{{ item.bikeCode }} · {{ item.level }}</strong>
          <div class="muted">{{ item.issue }}</div>
          <div class="muted">{{ item.storeName }} / {{ item.reportTime }}</div>
        </div>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" title="新增维修工单" width="760px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" v-loading="optionsLoading">
        <div class="section-grid cols-2">
          <el-form-item label="车辆" prop="bikeId">
            <el-select
              v-model="form.bikeId"
              placeholder="选择门店车辆"
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
                <div class="option-subtitle">{{ item.bikeName }} / 状态 {{ statusLabelMap[item.bikeStatus] || item.bikeStatus }}</div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="指派技师">
            <el-select
              v-model="form.assignedStaffId"
              placeholder="选择技师"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="item in staffOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
                <div class="option-title">{{ item.label }}</div>
                <div class="option-subtitle">{{ item.jobTitle || '门店员工' }}</div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="维修类型" prop="maintenanceType">
            <el-select v-model="form.maintenanceType" placeholder="选择维修类型" style="width: 100%">
              <el-option
                v-for="item in maintenanceTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="工单状态" prop="maintenanceStatus">
            <el-select v-model="form.maintenanceStatus" placeholder="选择状态" style="width: 100%">
              <el-option
                v-for="item in createStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="开始时间">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择开始时间"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="预计下次保养日期">
            <el-date-picker
              v-model="form.nextMaintenanceDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <el-form-item label="故障描述" prop="faultDescription">
          <el-input
            v-model="form.faultDescription"
            type="textarea"
            :rows="3"
            maxlength="150"
            show-word-limit
            placeholder="填写轮组、变速、刹车等故障现象"
          />
        </el-form-item>

        <el-form-item label="维修内容">
          <el-input
            v-model="form.maintenanceContent"
            type="textarea"
            :rows="3"
            maxlength="150"
            show-word-limit
            placeholder="填写检修计划、零件更换等说明"
          />
        </el-form-item>

        <div class="section-grid cols-2">
          <el-form-item label="预估费用">
            <el-input-number v-model="form.maintenanceCost" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" placeholder="可填写车辆外观、配件情况等" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确认建单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createStaffMaintenance,
  getStaffMaintenance,
  getStaffMaintenanceFormOptions,
  updateStaffMaintenance
} from '@/api/staff'

const loading = ref(false)
const saving = ref(false)
const optionsLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const list = ref([])
const bikeOptions = ref([])
const staffOptions = ref([])
const overview = ref({
  pendingJobs: 0,
  inProgress: 0,
  completedToday: 0,
  sparePartsStock: '-'
})

const statusLabelMap = {
  1: '可租赁',
  2: '已预订',
  3: '租赁中',
  4: '维修中',
  5: '已停用'
}

const maintenanceTypeOptions = [
  { label: '日常保养', value: '日常保养' },
  { label: '轮组检修', value: '轮组检修' },
  { label: '变速调校', value: '变速调校' },
  { label: '刹车检修', value: '刹车检修' },
  { label: '外观损伤', value: '外观损伤' },
  { label: '其他故障', value: '其他故障' }
]

const createStatusOptions = [
  { label: '待处理', value: 1 },
  { label: '维修中', value: 2 }
]

const form = reactive({
  bikeId: null,
  assignedStaffId: null,
  maintenanceType: '',
  faultDescription: '',
  maintenanceContent: '',
  maintenanceCost: 0,
  startTime: '',
  maintenanceStatus: 1,
  nextMaintenanceDate: '',
  remark: ''
})

const rules = {
  bikeId: [{ required: true, message: '请选择车辆', trigger: 'change' }],
  maintenanceType: [{ required: true, message: '请选择维修类型', trigger: 'change' }],
  faultDescription: [{ required: true, message: '请填写故障描述', trigger: 'blur' }],
  maintenanceStatus: [{ required: true, message: '请选择工单状态', trigger: 'change' }]
}

const urgentList = computed(() =>
  list.value.filter((item) => ['紧急', '中等'].includes(item.level)).slice(0, 3)
)

function resetForm() {
  Object.assign(form, {
    bikeId: null,
    assignedStaffId: null,
    maintenanceType: '',
    faultDescription: '',
    maintenanceContent: '',
    maintenanceCost: 0,
    startTime: '',
    maintenanceStatus: 1,
    nextMaintenanceDate: '',
    remark: ''
  })
}

function normalizeDateTime(value) {
  return value ? value.replace(' ', 'T') : null
}

function formatCurrentDateTime() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

async function loadData() {
  loading.value = true
  try {
    const data = await getStaffMaintenance()
    list.value = data.list
    overview.value = data.overview
  } finally {
    loading.value = false
  }
}

async function loadFormOptions() {
  optionsLoading.value = true
  try {
    const data = await getStaffMaintenanceFormOptions()
    bikeOptions.value = data.bikes || []
    staffOptions.value = data.staffs || []
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
    await createStaffMaintenance({
      bikeId: form.bikeId,
      assignedStaffId: form.assignedStaffId,
      maintenanceType: form.maintenanceType,
      faultDescription: form.faultDescription.trim(),
      maintenanceContent: form.maintenanceContent.trim(),
      maintenanceCost: form.maintenanceCost ?? 0,
      startTime: normalizeDateTime(form.startTime),
      maintenanceStatus: form.maintenanceStatus,
      nextMaintenanceDate: form.nextMaintenanceDate || null,
      remark: form.remark.trim()
    })
    ElMessage.success('维修工单已创建')
    dialogVisible.value = false
    await Promise.all([loadData(), loadFormOptions()])
  } finally {
    saving.value = false
  }
}

async function changeMaintenanceStatus(row, maintenanceStatus) {
  const payload = { maintenanceStatus }
  if (maintenanceStatus === 2) {
    payload.startTime = normalizeDateTime(row.startTime) || formatCurrentDateTime()
  }
  if (maintenanceStatus === 3) {
    payload.endTime = normalizeDateTime(row.endTime) || formatCurrentDateTime()
  }

  await updateStaffMaintenance(row.maintenanceId || row.id, payload)
  await Promise.all([loadData(), loadFormOptions()])
}

async function handleStart(row) {
  const confirmed = await ElMessageBox.confirm(
    `确定将维修工单 ${row.bikeCode} 标记为“维修中”吗？`,
    '开始维修',
    { type: 'warning' }
  ).then(() => true).catch(() => false)
  if (!confirmed) {
    return
  }

  await changeMaintenanceStatus(row, 2)
  ElMessage.success('维修工单已更新为维修中')
}

async function handleComplete(row) {
  const confirmed = await ElMessageBox.confirm(
    `确定将维修工单 ${row.bikeCode} 标记为“已完成”吗？完成后车辆会恢复为可租赁状态。`,
    '完成维修',
    { type: 'success' }
  ).then(() => true).catch(() => false)
  if (!confirmed) {
    return
  }

  await changeMaintenanceStatus(row, 3)
  ElMessage.success('维修工单已完成，车辆已恢复为可租赁')
}

onMounted(async () => {
  await Promise.all([loadData(), loadFormOptions()])
})
</script>

<style scoped>
.urgent-item {
  padding: 16px 0;
  border-bottom: 1px dashed rgba(15, 23, 42, 0.08);
}

.urgent-item:last-child {
  border-bottom: 0;
}

.urgent-item .muted {
  margin-top: 8px;
  line-height: 1.7;
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
