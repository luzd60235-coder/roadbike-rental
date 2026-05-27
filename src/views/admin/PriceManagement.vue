<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">价格管理</h1>
        <p class="page-subtitle">维护车型租赁策略，支持新增、编辑和删除价格方案。</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="loadData">刷新列表</el-button>
        <el-button type="primary" @click="openCreateDialog">新增价格策略</el-button>
      </div>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">平均日租金</div>
        <div class="metric-value">¥ {{ highlights.averageDailyRent }}</div>
        <div class="metric-trend">当前策略平均价格</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">最高租金车型</div>
        <div class="metric-value metric-long">{{ highlights.highestModel }}</div>
        <div class="metric-trend">价格上限车型</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">押金区间</div>
        <div class="metric-value">{{ highlights.depositRange }}</div>
        <div class="metric-trend">适配不同车型档位</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">已配车型</div>
        <div class="metric-value">{{ highlights.pricedModels }} / {{ highlights.totalModels }}</div>
        <div class="metric-trend">已配置价格的车型数量</div>
      </article>
    </section>

    <section class="content-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="车型" prop="model" min-width="200" />
        <el-table-column label="配置状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.hasPricing ? 'success' : 'warning'">
              {{ row.hasPricing ? '已配置' : '未配置' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="日租金" min-width="110">
          <template #default="{ row }">
            <span :class="{ 'text-muted': !row.hasPricing }">¥ {{ row.dailyRent || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="押金" min-width="110">
          <template #default="{ row }">
            <span :class="{ 'text-muted': !row.hasPricing }">¥ {{ row.deposit || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="超时费" min-width="120">
          <template #default="{ row }">
            <span :class="{ 'text-muted': !row.hasPricing }">¥ {{ row.overtime || '-' }}/小时</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updatedAt" min-width="160" />
        <el-table-column label="操作" min-width="140" fixed="right">
          <template #default="{ row }">
            <template v-if="row.hasPricing">
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" @click="openCreateDialogForModel(row)">去配置</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增价格策略' : '编辑价格策略'" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="车型" prop="modelId">
          <el-select
            v-model="form.modelId"
            placeholder="选择车型"
            style="width: 100%"
            :disabled="dialogMode === 'edit'"
          >
            <el-option
              v-for="item in modelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
              :disabled="item.status !== 1"
            />
          </el-select>
        </el-form-item>
        <div class="section-grid cols-2">
          <div>
            <el-form-item label="日租金" prop="dailyRent">
              <el-input-number v-model="form.dailyRent" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
            <el-form-item label="押金" prop="depositAmount">
              <el-input-number v-model="form.depositAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
            <el-form-item label="超时费 / 小时" prop="overtimeFeePerHour">
              <el-input-number v-model="form.overtimeFeePerHour" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </div>
          <div>
            <el-form-item label="生效时间" prop="effectiveFrom">
              <el-date-picker
                v-model="form.effectiveFrom"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择生效时间"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="失效时间">
              <el-date-picker
                v-model="form.effectiveTo"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="可留空表示长期有效"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="选择状态" style="width: 100%">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ dialogMode === 'create' ? '确认新增' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminPricing,
  deleteAdminPricing,
  getAdminPricing,
  getAdminPricingFormOptions,
  updateAdminPricing
} from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref()
const list = ref([])
const modelOptions = ref([])
const highlights = ref({
  averageDailyRent: 0,
  highestModel: '-',
  depositRange: '-',
  totalModels: 0,
  pricedModels: 0
})

const form = reactive({
  pricingId: null,
  modelId: null,
  dailyRent: 0,
  depositAmount: 0,
  overtimeFeePerHour: 0,
  effectiveFrom: '',
  effectiveTo: '',
  status: 1
})

const rules = {
  modelId: [{ required: true, message: '请选择车型', trigger: 'change' }],
  dailyRent: [{ required: true, message: '请输入日租金', trigger: 'change' }],
  depositAmount: [{ required: true, message: '请输入押金', trigger: 'change' }],
  overtimeFeePerHour: [{ required: true, message: '请输入超时费', trigger: 'change' }],
  effectiveFrom: [{ required: true, message: '请选择生效时间', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function normalizeDateTime(value) {
  return value ? value.replace(' ', 'T') : null
}

function formatDateTimeValue(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

function getDefaultEffectiveFrom() {
  return formatDateTimeValue(new Date())
}

function resetForm() {
  Object.assign(form, {
    pricingId: null,
    modelId: null,
    dailyRent: 0,
    depositAmount: 0,
    overtimeFeePerHour: 0,
    effectiveFrom: '',
    effectiveTo: '',
    status: 1
  })
}

async function loadData() {
  loading.value = true
  try {
    const [data, options] = await Promise.all([getAdminPricing(), getAdminPricingFormOptions()])
    list.value = data.list
    highlights.value = data.highlights
    modelOptions.value = options.models
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  dialogMode.value = 'create'
  resetForm()
  form.effectiveFrom = getDefaultEffectiveFrom()
  refreshModelOptions()
  dialogVisible.value = true
}

function openCreateDialogForModel(row) {
  dialogMode.value = 'create'
  resetForm()
  form.effectiveFrom = getDefaultEffectiveFrom()
  refreshModelOptions()
  nextTick(() => {
    form.modelId = row.modelId
  })
  dialogVisible.value = true
}

async function refreshModelOptions() {
  try {
    const options = await getAdminPricingFormOptions()
    modelOptions.value = options.models
  } catch (error) {
    console.error('加载车型选项失败:', error)
  }
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  Object.assign(form, {
    pricingId: row.pricingId ?? row.id,
    modelId: row.modelId,
    dailyRent: row.dailyRent,
    depositAmount: row.deposit,
    overtimeFeePerHour: row.overtime,
    effectiveFrom: row.effectiveFrom ? row.effectiveFrom.replace('T', ' ') : '',
    effectiveTo: row.effectiveTo ? row.effectiveTo.replace('T', ' ') : '',
    status: row.status ?? 1
  })
  refreshModelOptions()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload = {
      modelId: form.modelId,
      dailyRent: form.dailyRent,
      depositAmount: form.depositAmount,
      overtimeFeePerHour: form.overtimeFeePerHour,
      effectiveFrom: normalizeDateTime(form.effectiveFrom),
      effectiveTo: normalizeDateTime(form.effectiveTo),
      status: form.status
    }

    if (dialogMode.value === 'create') {
      await createAdminPricing(payload)
      ElMessage.success('价格策略已新增')
    } else {
      const pricingId = form.pricingId ?? form.id
      if (!pricingId) {
        ElMessage.error('未找到价格策略编号，请刷新后重试')
        saving.value = false
        return
      }
      await updateAdminPricing(pricingId, payload)
      ElMessage.success('价格策略已更新')
    }

    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  if (!row.hasPricing) {
    return
  }
  try {
    await ElMessageBox.confirm('确认删除该价格策略吗？', '删除价格策略', {
      type: 'warning'
    })
  } catch {
    return
  }

  await deleteAdminPricing(row.id)
  ElMessage.success('价格策略已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 12px;
}

.metric-long {
  font-size: 22px;
}

.text-muted {
  color: #909399;
}
</style>
