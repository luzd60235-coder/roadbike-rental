<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">个人中心</h1>
        <p class="page-subtitle">查看并维护个人资料，支持修改登录密码。</p>
      </div>
      <el-button type="primary" :loading="saving" @click="handleSubmit">保存资料</el-button>
    </section>

    <section class="section-grid cols-2 profile-layout">
      <article class="soft-card" v-loading="loading">
        <h2 class="section-title">基础信息</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item label="用户名">
              <el-input :model-value="profile.username || '-'" disabled />
            </el-form-item>

            <el-form-item label="会员等级">
              <el-input :model-value="memberLevelLabel(profile.memberLevel)" disabled />
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option v-for="item in genderOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item label="身份证号" prop="idCardNo">
              <el-input v-model="form.idCardNo" placeholder="请输入身份证号" />
            </el-form-item>

            <el-form-item label="账号状态">
              <el-input :model-value="profile.status === 1 ? '正常' : '已停用'" disabled />
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
          <h2 class="section-title">账号信息</h2>
          <div class="info-pair">
            <span class="muted">最近登录</span>
            <strong>{{ formatDateTime(profile.lastLoginAt) }}</strong>
          </div>
          <div class="info-pair">
            <span class="muted">注册时间</span>
            <strong>{{ formatDateTime(profile.createdAt) }}</strong>
          </div>
          <div class="info-pair">
            <span class="muted">当前角色</span>
            <strong>普通用户</strong>
          </div>
          <div class="info-pair">
            <span class="muted">联系方式</span>
            <strong>{{ profile.phone || '-' }}</strong>
          </div>
          <div class="info-pair">
            <span class="muted">邮箱地址</span>
            <strong>{{ profile.email || '-' }}</strong>
          </div>
        </article>

        <article class="soft-card">
          <h2 class="section-title">使用提示</h2>
          <div class="summary-list">
            <div class="summary-item">
              <span>修改密码后，下次登录将使用新密码。</span>
            </div>
            <div class="summary-item">
              <span>手机号与身份证号会用于租赁身份核验，请保持真实有效。</span>
            </div>
            <div class="summary-item">
              <span>昵称修改后，页面顶部展示名称会同步更新。</span>
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
import { getUserProfile, updateUserProfile } from '@/api/user'
import { updateCurrentUser } from '@/utils/auth'

const formRef = ref()
const loading = ref(false)
const saving = ref(false)

const profile = reactive({
  userId: null,
  username: '',
  nickname: '',
  gender: 0,
  phone: '',
  email: '',
  idCardNo: '',
  memberLevel: 'normal',
  status: 1,
  lastLoginAt: '',
  createdAt: ''
})

const form = reactive({
  nickname: '',
  gender: 0,
  phone: '',
  email: '',
  idCardNo: '',
  password: '',
  confirmPassword: ''
})

const genderOptions = [
  { label: '未知', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  idCardNo: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback(new Error('请输入身份证号'))
          return
        }
        if (!/^\d{17}[\dXx]$/.test(value.trim())) {
          callback(new Error('请输入正确的身份证号'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
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
    nickname: data.nickname || '',
    gender: data.gender ?? 0,
    phone: data.phone || '',
    email: data.email || '',
    idCardNo: data.idCardNo || '',
    password: '',
    confirmPassword: ''
  })
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ')
}

function memberLevelLabel(value) {
  return (
    {
      normal: '普通会员',
      silver: '白银会员',
      gold: '黄金会员',
      platinum: '白金会员'
    }[value] || value || '-'
  )
}

async function loadProfile() {
  loading.value = true
  try {
    const data = await getUserProfile()
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
    const data = await updateUserProfile({
      nickname: form.nickname.trim(),
      gender: form.gender,
      phone: form.phone.trim(),
      email: form.email.trim(),
      idCardNo: form.idCardNo.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword
    })

    assignProfile(data)
    updateCurrentUser({
      name: data.nickname || data.username,
      avatar: (data.nickname || data.username || '骑').slice(0, 1)
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
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(15, 118, 110, 0.06);
  color: var(--text-secondary);
  line-height: 1.7;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
