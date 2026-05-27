<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">个人中心</h1>
        <p class="page-subtitle">查看并维护门店员工个人资料，同时掌握当前门店与当班相关数据。</p>
      </div>
      <el-button type="primary" :loading="saving" @click="handleSubmit">保存资料</el-button>
    </section>

    <section class="metric-grid" v-loading="loading">
      <article class="metric-card">
        <div class="metric-label">待取车订单</div>
        <div class="metric-value">{{ profile.pendingPickupCount || 0 }}</div>
        <div class="metric-trend">当前门店等待交付车辆</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">待结算订单</div>
        <div class="metric-value">{{ profile.pendingSettlementCount || 0 }}</div>
        <div class="metric-trend">已还车、等待门店结算</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">维修处理中</div>
        <div class="metric-value">{{ profile.maintenanceInProgressCount || 0 }}</div>
        <div class="metric-trend">当前门店仍在处理的维修工单</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">今日建单</div>
        <div class="metric-value">{{ profile.createdOrdersToday || 0 }}</div>
        <div class="metric-trend">当前员工今日创建的线下订单</div>
      </article>
    </section>

    <section class="section-grid cols-2 profile-layout">
      <article class="soft-card" v-loading="loading">
        <h2 class="section-title">个人资料</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item label="用户名">
              <el-input :model-value="profile.username || '-'" disabled />
            </el-form-item>

            <el-form-item label="所属门店">
              <el-input :model-value="profile.storeName || '-'" disabled />
            </el-form-item>

            <el-form-item label="员工姓名" prop="staffName">
              <el-input v-model="form.staffName" placeholder="请输入员工姓名" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="岗位">
              <el-input :model-value="jobTitleLabel(profile.jobTitle)" disabled />
            </el-form-item>

            <el-form-item label="账号状态">
              <el-input :model-value="profile.status === 1 ? '启用中' : '已停用'" disabled />
            </el-form-item>

            <el-form-item label="新密码">
              <el-input v-model="form.password" show-password placeholder="不修改可留空" />
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" show-password placeholder="请再次输入新密码" />
            </el-form-item>
          </div>
        </el-form>
      </article>

      <div class="profile-side" v-loading="loading">
        <article class="soft-card">
          <h2 class="section-title">当班信息</h2>
          <div class="info-pair">
            <span class="muted">当前班次</span>
            <strong>{{ profile.currentShift || '-' }}</strong>
          </div>
          <div class="info-pair">
            <span class="muted">值班时段</span>
            <strong>{{ profile.shiftPeriod || '-' }}</strong>
          </div>
          <div class="info-pair">
            <span class="muted">工作时间</span>
            <strong>{{ profile.workSchedule || '-' }}</strong>
          </div>
          <div class="info-pair">
            <span class="muted">门店电话</span>
            <strong>{{ profile.storePhone || '-' }}</strong>
          </div>
          <div class="info-pair">
            <span class="muted">营业时间</span>
            <strong>{{ profile.businessHours || '-' }}</strong>
          </div>
          <div class="info-pair">
            <span class="muted">门店地址</span>
            <strong>{{ profile.storeAddress || '-' }}</strong>
          </div>
          <div class="info-pair">
            <span class="muted">最近登录</span>
            <strong>{{ formatDateTime(profile.lastLoginAt) }}</strong>
          </div>
          <div class="info-pair">
            <span class="muted">账号创建时间</span>
            <strong>{{ formatDateTime(profile.createdAt) }}</strong>
          </div>
        </article>

        <article class="soft-card">
          <h2 class="section-title">今日处理概览</h2>
          <div class="summary-list">
            <div class="summary-item">
              <span>租赁中车辆</span>
              <strong>{{ profile.rentingCount || 0 }}</strong>
            </div>
            <div class="summary-item">
              <span>今日完成结算</span>
              <strong>{{ profile.settlementsToday || 0 }}</strong>
            </div>
            <div class="summary-item">
              <span>今日完成维修</span>
              <strong>{{ profile.completedMaintenanceToday || 0 }}</strong>
            </div>
            <div class="summary-item">
              <span>当前岗位</span>
              <strong>{{ jobTitleLabel(profile.jobTitle) }}</strong>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getStaffProfile, updateStaffProfile } from '@/api/staff'
import { updateCurrentUser } from '@/utils/auth'

const formRef = ref()
const loading = ref(false)
const saving = ref(false)

const profile = reactive({
  staffId: null,
  username: '',
  staffName: '',
  phone: '',
  jobTitle: '',
  workSchedule: '',
  status: 1,
  storeId: null,
  storeName: '',
  storePhone: '',
  businessHours: '',
  storeAddress: '',
  currentShift: '',
  shiftPeriod: '',
  lastLoginAt: '',
  createdAt: '',
  pendingPickupCount: 0,
  rentingCount: 0,
  pendingSettlementCount: 0,
  maintenanceInProgressCount: 0,
  createdOrdersToday: 0,
  settlementsToday: 0,
  completedMaintenanceToday: 0
})

const form = reactive({
  staffName: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const jobTitleOptions = [
  { label: '店长', value: 'manager' },
  { label: '店员', value: 'clerk' },
  { label: '维修员', value: 'mechanic' }
]

const rules = {
  staffName: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
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

function assignProfile(data = {}) {
  Object.assign(profile, data)
  Object.assign(form, {
    staffName: data.staffName || '',
    phone: data.phone || '',
    password: '',
    confirmPassword: ''
  })
}

function jobTitleLabel(value) {
  return jobTitleOptions.find((item) => item.value === value)?.label || value || '-'
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ')
}

async function loadProfile() {
  loading.value = true
  try {
    const data = await getStaffProfile()
    assignProfile(data)
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const data = await updateStaffProfile({
      staffName: form.staffName.trim(),
      phone: form.phone.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword
    })

    assignProfile(data)
    updateCurrentUser({
      name: data.staffName || data.username,
      avatar: (data.staffName || data.username || '店').slice(0, 1),
      storeName: data.storeName || ''
    })
    ElMessage.success('个人资料已更新')
  } finally {
    saving.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-layout {
  align-items: start;
}

.profile-side {
  display: grid;
  gap: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.summary-list {
  display: grid;
  gap: 12px;
}

.summary-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(15, 118, 110, 0.06);
}

.summary-item span {
  color: var(--text-secondary);
}

.summary-item strong {
  font-size: 20px;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
