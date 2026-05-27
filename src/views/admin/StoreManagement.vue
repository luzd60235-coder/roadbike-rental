<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">门店管理</h1>
        <p class="page-subtitle">维护门店基础信息与服务能力，支持新增、编辑和删除。</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="loadData">刷新列表</el-button>
        <el-button type="primary" @click="openCreateDialog">新增门店</el-button>
      </div>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">门店总数</div>
        <div class="metric-value">{{ summary.totalStores }}</div>
        <div class="metric-trend">当前运营中的门店数量</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">平均利用率</div>
        <div class="metric-value">{{ summary.averageUtilization }}</div>
        <div class="metric-trend">各门店综合周转情况</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">活跃城市</div>
        <div class="metric-value">{{ summary.activeCities }}</div>
        <div class="metric-trend">已覆盖运营城市</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">服务覆盖率</div>
        <div class="metric-value">{{ summary.serviceCoverage }}</div>
        <div class="metric-trend">当前门店服务开通率</div>
      </article>
    </section>

    <section class="content-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="门店名称" prop="name" min-width="150" />
        <el-table-column label="城市区域" min-width="150">
          <template #default="{ row }">{{ row.city }} · {{ row.district }}</template>
        </el-table-column>
        <el-table-column label="地址 / 电话" min-width="260">
          <template #default="{ row }">
            <div>{{ row.address }}</div>
            <div class="muted">{{ row.phone }}</div>
          </template>
        </el-table-column>
        <el-table-column label="营业时间" prop="hours" min-width="140" />
        <el-table-column label="容量" min-width="110">
          <template #default="{ row }">{{ row.capacity }}</template>
        </el-table-column>
        <el-table-column label="库存利用率" min-width="170">
          <template #default="{ row }">
            <div class="utilization-cell">
              <el-progress :percentage="row.utilization" :stroke-width="10" />
              <div class="muted utilization-meta">
                当前库存 {{ row.inventoryCount }} / 容量 {{ row.capacity }} · 可租 {{ row.available }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增门店' : '编辑门店'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="section-grid cols-2">
          <div>
            <el-form-item label="门店名称" prop="storeName">
              <el-input v-model="form.storeName" placeholder="请输入门店名称" />
            </el-form-item>
            <el-form-item label="负责人">
              <el-input v-model="form.contactName" placeholder="请输入负责人姓名" />
            </el-form-item>
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="省份" prop="province">
              <el-select
                v-model="form.province"
                placeholder="选择省份"
                style="width: 100%"
                @change="handleProvinceChange"
              >
                <el-option
                  v-for="item in provinceOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="城市" prop="city">
              <el-select
                v-model="form.city"
                placeholder="选择城市"
                style="width: 100%"
                :disabled="!form.province"
                @change="handleCityChange"
              >
                <el-option
                  v-for="item in cityOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="区县" prop="district">
              <el-select
                v-model="form.district"
                placeholder="选择区县"
                style="width: 100%"
                :disabled="!form.city"
              >
                <el-option
                  v-for="item in districtOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>

          <div>
            <el-form-item label="详细地址" prop="detailAddress">
              <el-input v-model="form.detailAddress" placeholder="请输入街道、门牌号等详细地址" />
            </el-form-item>
            <el-form-item label="门店图片">
              <el-upload
                class="store-cover-uploader"
                :show-file-list="false"
                accept="image/jpeg,image/png,image/webp"
                :disabled="coverUploading"
                :before-upload="beforeCoverUpload"
                :http-request="handleCoverUpload"
              >
                <div v-if="form.storeImageUrl" class="cover-preview-wrapper">
                  <img :src="form.storeImageUrl" class="cover-preview" />
                  <div class="cover-actions">
                    <el-button
                      type="danger"
                      size="small"
                      circle
                      @click.stop="handleCoverRemove"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </div>
                <div v-else class="upload-placeholder">
                  <el-icon v-if="coverUploading" class="is-loading"><Loading /></el-icon>
                  <el-icon v-else><Plus /></el-icon>
                  <span>{{ coverUploading ? '上传中...' : '上传图片' }}</span>
                </div>
              </el-upload>
              <div class="form-tip">支持 JPG、PNG、WebP 格式，建议尺寸 800x600</div>
            </el-form-item>
            <el-form-item label="营业开始时间">
              <el-time-select
                v-model="form.businessHoursStart"
                placeholder="选择开始时间"
                start="06:00"
                step="00:30"
                end="23:30"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="营业结束时间">
              <el-time-select
                v-model="form.businessHoursEnd"
                placeholder="选择结束时间"
                start="06:00"
                step="00:30"
                end="23:30"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="库存容量" prop="inventoryCapacity">
              <el-input-number v-model="form.inventoryCapacity" :min="1" style="width: 100%" />
            </el-form-item>
            <el-form-item label="纬度">
              <el-input-number v-model="form.latitude" :precision="6" style="width: 100%" />
            </el-form-item>
            <el-form-item label="经度">
              <el-input-number v-model="form.longitude" :precision="6" style="width: 100%" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="选择状态" style="width: 100%">
                <el-option label="启用" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <el-form-item label="门店说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="120"
            show-word-limit
            placeholder="填写门店服务能力、适用骑行场景等"
          />
        </el-form-item>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Loading } from '@element-plus/icons-vue'
import { STORE_REGION_TREE, appendOptionIfMissing } from '@/constants/adminFormOptions'
import {
  createAdminStore,
  deleteAdminStore,
  getAdminStores,
  updateAdminStore,
  uploadAdminStoreCover
} from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const coverUploading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref()
const list = ref([])
const summary = ref({
  totalStores: 0,
  averageUtilization: '0%',
  activeCities: 0,
  serviceCoverage: '0%'
})

const form = reactive({
  storeId: null,
  storeName: '',
  contactName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  latitude: null,
  longitude: null,
  storeImageUrl: '',
  businessHoursStart: '',
  businessHoursEnd: '',
  inventoryCapacity: 20,
  description: '',
  status: 1
})

const rules = {
  storeName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  inventoryCapacity: [{ required: true, message: '请输入库存容量', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const provinceOptions = computed(() =>
  appendOptionIfMissing(
    STORE_REGION_TREE.map((item) => ({
      label: item.label,
      value: item.value
    })),
    form.province
  )
)

const cityOptions = computed(() => {
  const province = STORE_REGION_TREE.find((item) => item.value === form.province)
  const options = (province?.cities || []).map((item) => ({
    label: item.label,
    value: item.value
  }))

  return appendOptionIfMissing(options, form.city)
})

const districtOptions = computed(() => {
  const province = STORE_REGION_TREE.find((item) => item.value === form.province)
  const city = province?.cities?.find((item) => item.value === form.city)
  const options = (city?.districts || []).map((item) => ({
    label: item,
    value: item
  }))

  return appendOptionIfMissing(options, form.district)
})

function handleProvinceChange() {
  form.city = ''
  form.district = ''
}

function handleCityChange() {
  form.district = ''
}

function beforeCoverUpload(file) {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleCoverUpload(options) {
  coverUploading.value = true
  try {
    const imageUrl = await uploadAdminStoreCover(options.file)
    form.storeImageUrl = imageUrl
    options.onSuccess?.({ url: imageUrl })
    ElMessage.success('门店图片上传成功')
  } catch (error) {
    options.onError?.(error)
    ElMessage.error('图片上传失败')
  } finally {
    coverUploading.value = false
  }
}

function handleCoverRemove() {
  form.storeImageUrl = ''
}

function resetForm() {
  Object.assign(form, {
    storeId: null,
    storeName: '',
    contactName: '',
    contactPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    latitude: null,
    longitude: null,
    storeImageUrl: '',
    businessHoursStart: '',
    businessHoursEnd: '',
    inventoryCapacity: 20,
    description: '',
    status: 1
  })
}

async function loadData() {
  loading.value = true
  try {
    const data = await getAdminStores()
    list.value = data.list
    summary.value = data.summary
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  // 解析营业时间字符串
  let businessHoursStart = ''
  let businessHoursEnd = ''
  if (row.hours) {
    const match = row.hours.match(/(\d{2}:\d{2})\s*-\s*(\d{2}:\d{2})/)
    if (match) {
      businessHoursStart = match[1]
      businessHoursEnd = match[2]
    }
  }
  Object.assign(form, {
    storeId: row.id,
    storeName: row.name,
    contactName: row.contactName || '',
    contactPhone: row.phone || '',
    province: row.province || '',
    city: row.city || '',
    district: row.district || '',
    detailAddress: row.address || '',
    latitude: row.latitude ?? null,
    longitude: row.longitude ?? null,
    storeImageUrl: row.storeImageUrl || '',
    businessHoursStart,
    businessHoursEnd,
    inventoryCapacity: row.capacity || 20,
    description: row.description || '',
    status: row.statusCode ?? 1
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    // 组合营业时间
    const businessHours = form.businessHoursStart && form.businessHoursEnd
      ? `${form.businessHoursStart} - ${form.businessHoursEnd}`
      : ''

    const payload = {
      storeName: form.storeName.trim(),
      contactName: form.contactName.trim(),
      contactPhone: form.contactPhone.trim(),
      province: form.province.trim(),
      city: form.city.trim(),
      district: form.district.trim(),
      detailAddress: form.detailAddress.trim(),
      latitude: form.latitude,
      longitude: form.longitude,
      storeImageUrl: form.storeImageUrl || null,
      businessHours,
      inventoryCapacity: form.inventoryCapacity,
      description: form.description.trim(),
      status: form.status
    }

    if (dialogMode.value === 'create') {
      await createAdminStore(payload)
      ElMessage.success('门店已新增')
    } else {
      await updateAdminStore(form.storeId, payload)
      ElMessage.success('门店信息已更新')
    }

    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除门店“${row.name}”吗？`, '删除门店', {
      type: 'warning'
    })
  } catch {
    return
  }

  await deleteAdminStore(row.id)
  ElMessage.success('门店已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 12px;
}

.utilization-cell {
  min-width: 0;
}

.utilization-meta {
  margin-top: 6px;
}

.store-cover-uploader {
  width: 100%;
}

.store-cover-uploader :deep(.el-upload) {
  width: 100%;
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.store-cover-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 0;
  color: #909399;
}

.upload-placeholder .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.upload-placeholder span {
  font-size: 13px;
}

.cover-preview-wrapper {
  position: relative;
  width: 100%;
}

.cover-preview {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}

.cover-actions {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview-wrapper:hover .cover-actions {
  opacity: 1;
}

.cover-preview-wrapper::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview-wrapper:hover::after {
  opacity: 1;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}
</style>
