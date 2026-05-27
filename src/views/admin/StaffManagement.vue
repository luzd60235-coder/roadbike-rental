<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">门店员工管理</h1>
        <p class="page-subtitle">查看员工所属门店，统一维护岗位、工作时间和账号状态。</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="handleReset">重置筛选</el-button>
        <el-button type="primary" @click="loadData">刷新列表</el-button>
      </div>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">员工总数</div>
        <div class="metric-value">{{ summary.total }}</div>
        <div class="metric-trend">当前系统中的全部门店员工</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">在职员工</div>
        <div class="metric-value">{{ summary.active }}</div>
        <div class="metric-trend">状态为启用的门店员工</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">覆盖门店</div>
        <div class="metric-value">{{ summary.storeCount }}</div>
        <div class="metric-trend">当前已分配员工的门店数量</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">已排工作时间</div>
        <div class="metric-value">{{ summary.withSchedule }}</div>
        <div class="metric-trend">已配置个人工作时间的员工</div>
      </article>
    </section>

    <section class="toolbar-card">
      <div class="filters-row">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索员工姓名 / 账号 / 手机号 / 岗位"
          style="width: 320px"
        />
        <el-select v-model="storeId" clearable placeholder="选择门店" style="width: 220px">
          <el-option v-for="item in storeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="status" clearable placeholder="账号状态" style="width: 180px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>
    </section>

    <section class="content-card">
      <el-table :data="filteredList" v-loading="loading" stripe>
        <el-table-column label="员工信息" min-width="220">
          <template #default="{ row }">
            <div>{{ row.staffName }}</div>
            <div class="muted">{{ row.username }} / {{ row.phone || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="所属门店" min-width="220">
          <template #default="{ row }">
            <div>{{ row.storeName || '-' }}</div>
            <div class="muted">{{ [row.storeCity, row.storeDistrict].filter(Boolean).join(' / ') || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="岗位" min-width="120">
          <template #default="{ row }">{{ jobTitleLabel(row.jobTitle) }}</template>
        </el-table-column>

        <el-table-column label="工作时间" min-width="180">
          <template #default="{ row }">
            <div>{{ row.workSchedule || '按门店排班' }}</div>
            <div class="muted">门店营业：{{ row.businessHours || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="最近登录" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.lastLoginAt) }}</template>
        </el-table-column>

        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" title="编辑门店员工" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="员工账号">
            <el-input :model-value="form.username" disabled />
          </el-form-item>

          <el-form-item label="所属门店">
            <el-input :model-value="form.storeName" disabled />
          </el-form-item>

          <el-form-item label="员工姓名" prop="staffName">
            <el-input v-model="form.staffName" placeholder="请输入员工姓名" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item label="岗位" prop="jobTitle">
            <el-select v-model="form.jobTitle" placeholder="请选择岗位" style="width: 100%">
              <el-option v-for="item in jobTitleOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="账号状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="工作时间" prop="workSchedule">
          <el-input v-model="form.workSchedule" placeholder="例如 周一至周五 09:00 - 18:00" />
        </el-form-item>

        <div class="form-grid">
          <el-form-item label="新密码">
            <el-input v-model="form.password" show-password placeholder="不重置可留空" />
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" show-password placeholder="请再次输入新密码" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminStaffMembers, getAdminStores, updateAdminStaffMember } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const list = ref([])
const stores = ref([])
const keyword = ref('')
const storeId = ref(null)
const status = ref(null)

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const jobTitleOptions = [
  { label: '店长', value: 'manager' },
  { label: '店员', value: 'clerk' },
  { label: '维修员', value: 'mechanic' }
]

const form = reactive({
  staffId: null,
  username: '',
  storeName: '',
  staffName: '',
  phone: '',
  jobTitle: 'clerk',
  workSchedule: '',
  status: 1,
  password: '',
  confirmPassword: ''
})

const rules = {
  staffName: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  jobTitle: [{ required: true, message: '请选择岗位', trigger: 'change' }],
  workSchedule: [{ required: true, message: '请输入工作时间', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  confirmPassword: [
    {
      validator: (_rule, value, callback) => {
        if (form.password && value !== form.password) {
          callback(new Error('两次输入的新密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const storeOptions = computed(() =>
  stores.value.map((item) => ({
    label: item.name,
    value: item.id
  }))
)

const filteredList = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()

  return list.value.filter((item) => {
    const searchText = [
      item.staffName,
      item.username,
      item.phone,
      item.jobTitle,
      item.storeName
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()

    const matchKeyword = !normalizedKeyword || searchText.includes(normalizedKeyword)
    const matchStore = !storeId.value || item.storeId === storeId.value
    const matchStatus = status.value == null || item.status === status.value
    return matchKeyword && matchStore && matchStatus
  })
})

const summary = computed(() => ({
  total: filteredList.value.length,
  active: filteredList.value.filter((item) => item.status === 1).length,
  storeCount: new Set(filteredList.value.map((item) => item.storeId).filter(Boolean)).size,
  withSchedule: filteredList.value.filter((item) => !!item.workSchedule).length
}))

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ')
}

function jobTitleLabel(value) {
  return jobTitleOptions.find((item) => item.value === value)?.label || value || '-'
}

async function loadData() {
  loading.value = true
  try {
    const [staffData, storeData] = await Promise.all([getAdminStaffMembers(), getAdminStores()])
    list.value = staffData.list
    stores.value = storeData.list
  } finally {
    loading.value = false
  }
}

function handleReset() {
  keyword.value = ''
  storeId.value = null
  status.value = null
}

async function openEditDialog(row) {
  Object.assign(form, {
    staffId: row.staffId,
    username: row.username || '',
    storeName: row.storeName || '',
    staffName: row.staffName || '',
    phone: row.phone || '',
    jobTitle: row.jobTitle || 'clerk',
    workSchedule: row.workSchedule || row.businessHours || '',
    status: row.status ?? 1,
    password: '',
    confirmPassword: ''
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate?.()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !form.staffId) {
    return
  }

  saving.value = true
  try {
    await updateAdminStaffMember(form.staffId, {
      staffName: form.staffName.trim(),
      phone: form.phone.trim(),
      jobTitle: form.jobTitle,
      workSchedule: form.workSchedule.trim(),
      status: form.status,
      password: form.password,
      confirmPassword: form.confirmPassword
    })
    ElMessage.success('门店员工信息已更新')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
