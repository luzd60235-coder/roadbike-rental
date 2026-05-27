<template>
  <div class="login-shell">
    <section class="login-stage">
      <div class="login-stage-inner">
        <section class="stage-visual">
          <img class="stage-image" :src="loginHeroImage" alt="门店公路车展示实景" />
          <div class="stage-overlay"></div>

          <div class="stage-copy">
            <div class="copy-top">
              <span class="copy-chip">都市圈公路车租赁服务系统</span>
            </div>

            <div class="visual-metrics">
              <div v-for="item in visualMetrics" :key="item.label" class="visual-metric">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </div>
        </section>

        <section class="login-panel">
          <div class="panel-head">
            <div class="panel-kicker">
              <span class="panel-kicker-line"></span>
              <span>{{ authMode === 'login' ? '进入系统' : '创建账号' }}</span>
            </div>
            <h2>{{ authMode === 'login' ? '欢迎登录' : '注册账号' }}</h2>
          </div>

          <div class="mode-switch">
            <button
              type="button"
              :class="['mode-chip', { active: authMode === 'login' }]"
              @click="switchMode('login')"
            >
              登录
            </button>
            <button
              type="button"
              :class="['mode-chip', { active: authMode === 'register' }]"
              @click="switchMode('register')"
            >
              注册
            </button>
          </div>

          <div class="role-selector">
            <button
              v-for="role in roleOptions"
              :key="role.value"
              type="button"
              :class="['role-card', { active: activeRole === role.value }]"
              @click="changeRole(role.value)"
            >
              <strong>{{ role.label }}</strong>
              <span>{{ role.desc }}</span>
            </button>
          </div>

          <div class="panel-form">
            <el-form
              v-if="authMode === 'login'"
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              label-position="top"
              @submit.prevent
            >
              <el-form-item label="用户名" prop="username">
                <el-input v-model="loginForm.username" size="large" placeholder="请输入用户名" />
              </el-form-item>

              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="loginForm.password"
                  size="large"
                  show-password
                  placeholder="请输入密码"
                />
              </el-form-item>

              <el-button type="primary" size="large" class="submit-button" :loading="loading" @click="handleLogin">
                进入系统
              </el-button>
            </el-form>

            <el-form
              v-else
              ref="registerFormRef"
              :model="registerForm"
              :rules="registerRules"
              label-position="top"
              @submit.prevent
            >
              <div class="form-grid">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="registerForm.username" size="large" placeholder="请设置用户名" />
                </el-form-item>

                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="registerForm.phone" size="large" placeholder="请输入手机号" />
                </el-form-item>

                <el-form-item v-if="activeRole !== 'admin'" label="性别" prop="gender">
                  <el-select v-model="registerForm.gender" size="large" placeholder="请选择性别" style="width: 100%">
                    <el-option v-for="item in genderOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>

                <el-form-item label="身份证号" prop="idCardNo">
                  <el-input v-model="registerForm.idCardNo" size="large" placeholder="请输入身份证号" />
                </el-form-item>

                <el-form-item v-if="activeRole === 'user'" label="昵称" prop="nickname">
                  <el-input v-model="registerForm.nickname" size="large" placeholder="请输入昵称" />
                </el-form-item>

                <el-form-item v-if="activeRole === 'admin'" label="姓名" prop="realName">
                  <el-input v-model="registerForm.realName" size="large" placeholder="请输入管理员姓名" />
                </el-form-item>

                <el-form-item v-if="activeRole === 'staff'" label="员工姓名" prop="staffName">
                  <el-input v-model="registerForm.staffName" size="large" placeholder="请输入员工姓名" />
                </el-form-item>

                <el-form-item v-if="activeRole !== 'staff'" label="邮箱" prop="email">
                  <el-input v-model="registerForm.email" size="large" placeholder="请输入邮箱（选填）" />
                </el-form-item>

                <el-form-item v-if="activeRole === 'staff'" label="所属门店" prop="storeId">
                  <el-select
                    v-model="registerForm.storeId"
                    size="large"
                    placeholder="请选择门店"
                    filterable
                    :loading="storeLoading"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="item in staffStoreOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item v-if="activeRole === 'staff'" label="岗位" prop="jobTitle">
                  <el-select v-model="registerForm.jobTitle" size="large" placeholder="请选择岗位" style="width: 100%">
                    <el-option
                      v-for="item in jobTitleOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item label="密码" prop="password">
                  <el-input v-model="registerForm.password" size="large" show-password placeholder="请设置密码" />
                </el-form-item>

                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="registerForm.confirmPassword"
                    size="large"
                    show-password
                    placeholder="请再次输入密码"
                  />
                </el-form-item>

                <el-form-item label="验证码" prop="verificationCode">
                  <div class="captcha-field">
                    <el-input
                      v-model="registerForm.verificationCode"
                      size="large"
                      placeholder="请输入验证码"
                    />
                    <button type="button" class="captcha-box" @click="refreshCaptcha">
                      <span>{{ captchaCode }}</span>
                      <small>点击刷新</small>
                    </button>
                  </div>
                </el-form-item>
              </div>

              <el-button type="primary" size="large" class="submit-button" :loading="loading" @click="handleRegister">
                立即注册
              </el-button>
            </el-form>
          </div>
        </section>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getStaffRegisterStores, login, register } from '@/api/auth'
import loginHeroImage from '@/assets/login/login-bike-store.jpg'
import { getHomePathByRole, setAuthSession } from '@/utils/auth'

const router = useRouter()
const loginFormRef = ref()
const registerFormRef = ref()
const loading = ref(false)
const storeLoading = ref(false)
const authMode = ref('login')
const activeRole = ref('admin')
const staffStoreOptions = ref([])
const captchaCode = ref(generateCaptcha())

const visualMetrics = [
  { label: '覆盖门店', value: '4 家' },
  { label: '在营车辆', value: '10 台' },
  { label: '本月订单', value: '6 单' }
]

const roleOptions = [
  {
    value: 'admin',
    label: '管理员',
    desc: '后台配置与运营维护'
  },
  {
    value: 'user',
    label: '普通用户',
    desc: '在线预约与订单查看'
  },
  {
    value: 'staff',
    label: '门店员工',
    desc: '交易结算与维修处理'
  }
]

const genderOptions = [
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

const jobTitleOptions = [
  { label: '店长', value: 'manager' },
  { label: '店员', value: 'clerk' },
  { label: '维修员', value: 'mechanic' }
]

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  realName: '',
  staffName: '',
  phone: '',
  email: '',
  gender: 1,
  idCardNo: '',
  storeId: null,
  jobTitle: 'clerk',
  verificationCode: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function validateIdCard(_rule, value, callback) {
  if (!value) {
    callback(new Error('请输入身份证号'))
    return
  }

  if (!/^\d{17}[\dXx]$/.test(value.trim())) {
    callback(new Error('请输入正确的身份证号'))
    return
  }

  callback()
}

function validateConfirmPassword(_rule, value, callback) {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }

  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }

  callback()
}

function validateCaptcha(_rule, value, callback) {
  if (!value) {
    callback(new Error('请输入验证码'))
    return
  }

  if (value.trim().toUpperCase() !== captchaCode.value) {
    callback(new Error('验证码不正确'))
    return
  }

  callback()
}

const registerRules = computed(() => {
  const rules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
    phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
    idCardNo: [{ validator: validateIdCard, trigger: 'blur' }],
    verificationCode: [{ validator: validateCaptcha, trigger: 'blur' }]
  }

  if (activeRole.value === 'user') {
    rules.nickname = [{ required: true, message: '请输入昵称', trigger: 'blur' }]
    rules.gender = [{ required: true, message: '请选择性别', trigger: 'change' }]
  }

  if (activeRole.value === 'admin') {
    rules.realName = [{ required: true, message: '请输入管理员姓名', trigger: 'blur' }]
  }

  if (activeRole.value === 'staff') {
    rules.staffName = [{ required: true, message: '请输入员工姓名', trigger: 'blur' }]
    rules.gender = [{ required: true, message: '请选择性别', trigger: 'change' }]
    rules.storeId = [{ required: true, message: '请选择所属门店', trigger: 'change' }]
    rules.jobTitle = [{ required: true, message: '请选择岗位', trigger: 'change' }]
  }

  return rules
})

function generateCaptcha() {
  const source = '23456789ABCDEFGHJKLMNPQRSTUVWXYZ'
  return Array.from({ length: 4 }, () => source[Math.floor(Math.random() * source.length)]).join('')
}

function refreshCaptcha() {
  captchaCode.value = generateCaptcha()
  registerForm.verificationCode = ''
  registerFormRef.value?.clearValidate?.(['verificationCode'])
}

function resetLoginForm() {
  loginForm.username = ''
  loginForm.password = ''
}

function resetRegisterForm() {
  Object.assign(registerForm, {
    username: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    realName: '',
    staffName: '',
    phone: '',
    email: '',
    gender: 1,
    idCardNo: '',
    storeId: null,
    jobTitle: 'clerk',
    verificationCode: ''
  })
}

async function ensureStaffStores() {
  if (staffStoreOptions.value.length > 0 || storeLoading.value) {
    return
  }

  storeLoading.value = true
  try {
    staffStoreOptions.value = await getStaffRegisterStores()
  } finally {
    storeLoading.value = false
  }
}

watch(
  [authMode, activeRole],
  async ([mode, role]) => {
    if (mode === 'login') {
      resetLoginForm()
    } else {
      resetRegisterForm()
      refreshCaptcha()
      if (role === 'staff') {
        await ensureStaffStores()
      }
    }

    await nextTick()
    loginFormRef.value?.clearValidate?.()
    registerFormRef.value?.clearValidate?.()
  },
  { immediate: true }
)

function switchMode(mode) {
  authMode.value = mode
}

function changeRole(role) {
  activeRole.value = role
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  loading.value = true
  try {
    const result = await login({
      role: activeRole.value,
      username: loginForm.username.trim(),
      password: loginForm.password
    })

    setAuthSession(result)
    ElMessage.success(`欢迎回来，${result.user.name}`)
    router.replace(getHomePathByRole(result.user.role))
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  loading.value = true
  try {
    const result = await register({
      role: activeRole.value,
      ...registerForm,
      username: registerForm.username.trim(),
      phone: registerForm.phone.trim(),
      email: registerForm.email.trim(),
      nickname: registerForm.nickname.trim(),
      realName: registerForm.realName.trim(),
      staffName: registerForm.staffName.trim(),
      idCardNo: registerForm.idCardNo.trim()
    })

    setAuthSession(result)
    ElMessage.success('注册成功，已进入系统')
    router.replace(getHomePathByRole(result.user.role))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-shell {
  min-height: 100vh;
  padding: 18px;
  background:
    radial-gradient(circle at top left, rgba(45, 95, 96, 0.14), transparent 28%),
    radial-gradient(circle at bottom right, rgba(186, 148, 92, 0.16), transparent 26%),
    linear-gradient(180deg, #edf1f3 0%, #f4f0ea 100%);
  overflow: hidden;
}

.login-stage {
  max-width: 1580px;
  height: calc(100vh - 36px);
  margin: 0 auto;
  padding: 16px;
  border-radius: 34px;
  background: rgba(255, 255, 255, 0.56);
  box-shadow: 0 30px 70px rgba(30, 41, 59, 0.14);
  backdrop-filter: blur(14px);
}

.login-stage-inner {
  display: grid;
  grid-template-columns: minmax(0, 1.16fr) minmax(410px, 460px);
  gap: 18px;
  height: 100%;
}

.stage-visual,
.login-panel {
  position: relative;
  overflow: hidden;
  border-radius: 28px;
}

.stage-visual {
  min-width: 0;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.18);
}

.stage-image,
.stage-overlay {
  position: absolute;
  inset: 0;
}

.stage-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.stage-overlay {
  background:
    linear-gradient(130deg, rgba(22, 30, 39, 0.74) 4%, rgba(22, 30, 39, 0.38) 38%, rgba(22, 30, 39, 0.08) 68%, rgba(22, 30, 39, 0.28) 100%),
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.14), transparent 26%);
}

.stage-copy {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  padding: 34px 34px 30px;
  color: #f8fafc;
}

.copy-chip {
  display: inline-flex;
  align-items: center;
  padding: 10px 16px;
  border-radius: 999px;
  color: rgba(248, 250, 252, 0.96);
  font-size: 13px;
  letter-spacing: 0.08em;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(10px);
}

.visual-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 24px;
}

.visual-metric {
  padding: 18px 18px 20px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.16), rgba(255, 255, 255, 0.09));
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(16px);
}

.visual-metric span {
  display: block;
  color: rgba(241, 245, 249, 0.84);
  font-size: 14px;
}

.visual-metric strong {
  display: block;
  margin-top: 12px;
  font-size: 42px;
  line-height: 1;
}

.login-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 24px 24px 22px;
  background: linear-gradient(180deg, rgba(255, 251, 247, 0.96) 0%, rgba(250, 247, 243, 0.92) 100%);
  border: 1px solid rgba(255, 255, 255, 0.88);
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.14);
  backdrop-filter: blur(18px);
}

.login-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at top right, rgba(15, 118, 110, 0.08), transparent 30%),
    radial-gradient(circle at bottom left, rgba(180, 128, 64, 0.08), transparent 32%);
  pointer-events: none;
}

.panel-head,
.mode-switch,
.role-selector,
.panel-form {
  position: relative;
  z-index: 1;
}

.panel-head {
  display: grid;
  gap: 8px;
}

.panel-kicker {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #52606d;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.panel-kicker-line {
  width: 28px;
  height: 1px;
  background: linear-gradient(90deg, #0f766e, rgba(15, 118, 110, 0.15));
}

.panel-head h2 {
  margin: 0;
  font-size: 34px;
  line-height: 1.04;
  color: #18212b;
}

.mode-switch {
  display: inline-flex;
  width: fit-content;
  margin-top: 18px;
  padding: 4px;
  background: rgba(15, 23, 42, 0.06);
  border-radius: 999px;
}

.mode-chip {
  min-width: 88px;
  padding: 9px 18px;
  border: 0;
  border-radius: 999px;
  color: #5d6875;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mode-chip.active {
  color: #ffffff;
  background: linear-gradient(135deg, #0f766e, #1d4ed8);
  box-shadow: 0 10px 22px rgba(29, 78, 216, 0.22);
}

.role-selector {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin: 16px 0 14px;
}

.role-card {
  padding: 14px 12px;
  text-align: left;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.2s ease;
}

.role-card strong,
.role-card span {
  display: block;
}

.role-card strong {
  color: #18212b;
  font-size: 14px;
}

.role-card span {
  margin-top: 5px;
  color: #667281;
  font-size: 12px;
  line-height: 1.45;
}

.role-card.active {
  border-color: rgba(15, 118, 110, 0.22);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(233, 244, 242, 0.96));
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
  transform: translateY(-1px);
}

.panel-form {
  flex: 1;
  min-height: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
}

.captcha-field {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 124px;
  gap: 10px;
  width: 100%;
}

.captcha-box {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 14px;
  color: #0f172a;
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.1), rgba(29, 78, 216, 0.1));
  cursor: pointer;
}

.captcha-box span {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.captcha-box small {
  color: #64748b;
  font-size: 11px;
}

.submit-button {
  width: 100%;
  margin-top: 6px;
}

:deep(.el-form-item) {
  margin-bottom: 12px;
}

:deep(.el-form-item__label) {
  padding-bottom: 4px;
  color: #334155;
  line-height: 1.35;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  border-radius: 14px;
  box-shadow: none;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.08);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focused) {
  border-color: rgba(15, 118, 110, 0.28);
  box-shadow: 0 0 0 4px rgba(15, 118, 110, 0.08);
}

:deep(.el-button--primary) {
  border: 0;
  background: linear-gradient(135deg, #0f766e 0%, #1d4ed8 100%);
  box-shadow: 0 16px 28px rgba(29, 78, 216, 0.18);
}

@media (max-width: 1380px) {
  .visual-metric strong {
    font-size: 36px;
  }
}

@media (max-height: 920px) and (min-width: 1101px) {
  .login-shell {
    padding: 14px;
  }

  .login-stage {
    height: calc(100vh - 28px);
    padding: 14px;
  }

  .stage-copy {
    padding: 28px 28px 24px;
  }

  .visual-metric {
    padding: 16px;
  }

  .visual-metric strong {
    font-size: 32px;
  }

  .login-panel {
    padding: 20px 20px 18px;
  }

  .panel-head h2 {
    font-size: 30px;
  }

  .mode-switch {
    margin-top: 14px;
  }

  .role-selector {
    margin: 12px 0;
    gap: 8px;
  }

  .role-card {
    padding: 12px 10px;
  }

  :deep(.el-form-item) {
    margin-bottom: 10px;
  }
}

@media (max-width: 1100px) {
  .login-shell {
    min-height: auto;
    padding: 12px;
    overflow: visible;
  }

  .login-stage {
    height: auto;
    min-height: calc(100vh - 24px);
    padding: 12px;
  }

  .login-stage-inner {
    grid-template-columns: 1fr;
    height: auto;
  }

  .stage-visual {
    min-height: 360px;
  }

  .stage-copy {
    padding: 26px 24px 22px;
  }

  .login-panel {
    min-height: 0;
  }
}

@media (max-width: 720px) {
  .visual-metrics,
  .role-selector,
  .form-grid,
  .captcha-field {
    grid-template-columns: 1fr;
  }

  .visual-metric strong {
    font-size: 30px;
  }

  .panel-head h2 {
    font-size: 28px;
  }
}
</style>
