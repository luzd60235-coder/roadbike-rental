<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">车辆管理</h1>
        <p class="page-subtitle">集中维护真实库存车辆，支持新增、编辑、删除和状态筛选。</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="loadData">刷新列表</el-button>
        <el-button type="primary" @click="openCreateDialog">新增车辆</el-button>
      </div>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">车辆总数</div>
        <div class="metric-value">{{ summary.totalBikes }}</div>
        <div class="metric-trend">当前库存中的全部车辆</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">可租车辆</div>
        <div class="metric-value">{{ summary.available }}</div>
        <div class="metric-trend">可以直接预约或下单的车辆</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">租赁中</div>
        <div class="metric-value">{{ summary.renting }}</div>
        <div class="metric-trend">正在履约中的库存车辆</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">维修中</div>
        <div class="metric-value">{{ summary.maintenance }}</div>
        <div class="metric-trend">待维修或维修中的车辆</div>
      </article>
    </section>

    <section class="toolbar-card">
      <div class="filters-row">
        <el-input
          v-model="keyword"
          placeholder="搜索品牌 / 车型 / 编码 / 车架号"
          clearable
          style="width: 320px"
        />
        <el-select v-model="status" placeholder="车辆状态" clearable style="width: 180px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="storeId" placeholder="所在门店" clearable style="width: 220px">
          <el-option v-for="item in storeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>
    </section>

    <section class="content-card">
      <el-table :data="filteredList" v-loading="loading" stripe>
        <el-table-column label="车辆编码" prop="code" min-width="150" />

        <el-table-column label="车型信息" min-width="280">
          <template #default="{ row }">
            <div>{{ row.bikeName }}</div>
            <div class="muted">{{ row.type }} / {{ row.size }} / {{ row.color }}</div>
          </template>
        </el-table-column>

        <el-table-column label="门店" min-width="180">
          <template #default="{ row }">
            <div>{{ row.storeName || '未分配门店' }}</div>
            <div class="muted">{{ row.city || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="价格" min-width="120">
          <template #default="{ row }">¥ {{ row.price || 0 }}</template>
        </el-table-column>

        <el-table-column label="累计里程" min-width="120">
          <template #default="{ row }">{{ row.mileage || 0 }} km</template>
        </el-table-column>

        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.statusType">{{ row.status }}</el-tag>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增车辆' : '编辑车辆'"
      width="760px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="section-grid cols-2">
          <div>
            <el-form-item label="车辆编码" prop="bikeCode">
              <el-input v-model="form.bikeCode" placeholder="例如 RB-2026-011" />
            </el-form-item>

            <el-form-item label="车型" prop="modelId">
              <div class="model-select-row">
                <el-select v-model="form.modelId" filterable placeholder="选择车型" style="width: 100%">
                  <el-option
                    v-for="item in modelOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                    :disabled="item.status !== 1"
                  />
                </el-select>
                <el-button plain @click="openModelDialog">新增车型</el-button>
              </div>
            </el-form-item>

            <el-form-item label="车架编号" prop="frameNo">
              <el-input v-model="form.frameNo" placeholder="请输入唯一车架编号" />
            </el-form-item>

            <el-form-item label="车辆尺码" prop="frameSize">
              <el-select v-model="form.frameSize" placeholder="选择车辆尺码" style="width: 100%">
                <el-option v-for="item in frameSizeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="颜色" prop="color">
              <el-select v-model="form.color" placeholder="选择车辆颜色" style="width: 100%">
                <el-option v-for="item in colorOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="所在门店" prop="currentStoreId">
              <el-select v-model="form.currentStoreId" placeholder="选择门店" style="width: 100%">
                <el-option v-for="item in storeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </div>

          <div>
            <el-form-item label="车辆状态" prop="bikeStatus">
              <el-select v-model="form.bikeStatus" placeholder="选择状态" style="width: 100%">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="采购日期">
              <el-date-picker
                v-model="form.purchaseDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择采购日期"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="采购价格">
              <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>

            <el-form-item label="累计里程">
              <el-input-number v-model="form.mileageKm" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>

            <el-form-item label="GPS 设备编号">
              <el-input v-model="form.gpsDeviceNo" placeholder="可选" />
            </el-form-item>
          </div>
        </div>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="100"
            show-word-limit
            placeholder="可填写调拨、使用情况或其他补充说明"
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

    <el-dialog
      v-model="modelDialogVisible"
      title="新增车型"
      width="760px"
      destroy-on-close
      append-to-body
    >
      <el-form ref="modelFormRef" :model="modelForm" :rules="modelRules" label-position="top">
        <div class="section-grid cols-2">
          <div>
            <el-form-item label="品牌" prop="brandName">
              <el-input v-model="modelForm.brandName" placeholder="例如 Specialized" />
            </el-form-item>

            <el-form-item label="系列">
              <el-input v-model="modelForm.seriesName" placeholder="例如 Tarmac" />
            </el-form-item>

            <el-form-item label="车型名称" prop="modelName">
              <el-input v-model="modelForm.modelName" placeholder="例如 SL8 Comp" />
            </el-form-item>

            <el-form-item label="车辆类型" prop="bikeType">
              <el-select
                v-model="modelForm.bikeType"
                filterable
                allow-create
                default-first-option
                clearable
                placeholder="可选择或输入，例如 爬坡型"
                style="width: 100%"
              >
                <el-option
                  v-for="item in bikeTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="车架材质">
              <el-input v-model="modelForm.frameMaterial" placeholder="例如 碳纤维" />
            </el-form-item>

            <el-form-item label="变速系统">
              <el-input v-model="modelForm.gearSystem" placeholder="例如 Shimano 105 24 速" />
            </el-form-item>
          </div>

          <div>
            <el-form-item label="制动类型">
              <el-input v-model="modelForm.brakeType" placeholder="例如 液压碟刹" />
            </el-form-item>

            <el-form-item label="轮组规格">
              <el-input v-model="modelForm.wheelSize" placeholder="例如 700C" />
            </el-form-item>

            <el-form-item label="市场价格">
              <el-input-number v-model="modelForm.marketPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>

            <el-form-item label="车型图片">
              <el-upload
                class="cover-uploader"
                accept="image/png,image/jpeg,image/webp"
                :show-file-list="false"
                :before-upload="beforeCoverUpload"
                :http-request="handleCoverUpload"
              >
                <img
                  v-if="modelForm.coverImageUrl"
                  :src="modelForm.coverImageUrl"
                  alt="车型图片"
                  class="cover-preview"
                />
                <div v-else class="cover-placeholder">
                  <span>{{ coverUploading ? '上传中...' : '点击上传车型图片' }}</span>
                  <small>支持 JPG / PNG / WEBP，大小不超过 5MB</small>
                </div>
              </el-upload>
            </el-form-item>
          </div>
        </div>

        <el-form-item label="车型说明">
          <el-input
            v-model="modelForm.description"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="可填写适用场景、车型亮点或展示文案"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="modelDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="modelSaving" @click="handleCreateModel">
          保存车型并选中
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  BIKE_COLOR_OPTIONS,
  BIKE_FRAME_SIZE_OPTIONS,
  BIKE_TYPE_OPTIONS,
  appendOptionIfMissing,
  normalizeBikeColor,
  normalizeBikeFrameSize
} from '@/constants/adminFormOptions'
import {
  createAdminBikeModel,
  createAdminVehicle,
  deleteAdminVehicle,
  getAdminVehicleFormOptions,
  getAdminVehicles,
  updateAdminVehicle,
  uploadAdminBikeCover
} from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref()
const keyword = ref('')
const status = ref('')
const storeId = ref(null)
const list = ref([])
const modelOptions = ref([])
const storeOptions = ref([])
const summary = ref({
  totalBikes: 0,
  available: 0,
  renting: 0,
  maintenance: 0
})

const modelDialogVisible = ref(false)
const modelSaving = ref(false)
const coverUploading = ref(false)
const modelFormRef = ref()

const statusOptions = [
  { label: '可租赁', value: 1 },
  { label: '已预订', value: 2 },
  { label: '租赁中', value: 3 },
  { label: '维修中', value: 4 },
  { label: '已停用', value: 5 }
]

const form = reactive({
  bikeId: null,
  bikeCode: '',
  modelId: null,
  frameNo: '',
  frameSize: '',
  color: '',
  currentStoreId: null,
  bikeStatus: 1,
  purchaseDate: '',
  purchasePrice: 0,
  mileageKm: 0,
  gpsDeviceNo: '',
  remark: ''
})

const modelForm = reactive({
  brandName: '',
  seriesName: '',
  modelName: '',
  bikeType: '',
  frameMaterial: '',
  gearSystem: '',
  brakeType: '',
  wheelSize: '700C',
  marketPrice: 0,
  coverImageUrl: '',
  description: '',
  status: 1
})

const rules = {
  bikeCode: [{ required: true, message: '请输入车辆编码', trigger: 'blur' }],
  modelId: [{ required: true, message: '请选择车型', trigger: 'change' }],
  frameNo: [{ required: true, message: '请输入车架编号', trigger: 'blur' }],
  frameSize: [{ required: true, message: '请选择车辆尺码', trigger: 'change' }],
  color: [{ required: true, message: '请选择车辆颜色', trigger: 'change' }],
  currentStoreId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  bikeStatus: [{ required: true, message: '请选择车辆状态', trigger: 'change' }]
}

const modelRules = {
  brandName: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入车型名称', trigger: 'blur' }],
  bikeType: [{ required: true, message: '请选择或输入车辆类型', trigger: 'change' }]
}

const frameSizeOptions = computed(() =>
  appendOptionIfMissing(BIKE_FRAME_SIZE_OPTIONS, normalizeBikeFrameSize(form.frameSize))
)

const colorOptions = computed(() =>
  appendOptionIfMissing(BIKE_COLOR_OPTIONS, normalizeBikeColor(form.color))
)

const bikeTypeOptions = computed(() => {
  let options = appendOptionIfMissing(BIKE_TYPE_OPTIONS, modelForm.bikeType)

  for (const item of modelOptions.value) {
    options = appendOptionIfMissing(options, item.bikeType)
  }

  return options
})

const filteredList = computed(() =>
  list.value.filter((item) => {
    const normalizedKeyword = keyword.value.trim().toLowerCase()
    const matchKeyword =
      !normalizedKeyword ||
      String(item.bikeName || '').toLowerCase().includes(normalizedKeyword) ||
      String(item.brand || '').toLowerCase().includes(normalizedKeyword) ||
      String(item.code || '').toLowerCase().includes(normalizedKeyword) ||
      String(item.frameNo || '').toLowerCase().includes(normalizedKeyword)
    const matchStatus = !status.value || item.statusCode === status.value
    const matchStore = !storeId.value || item.storeId === storeId.value
    return matchKeyword && matchStatus && matchStore
  })
)

function normalizeText(value) {
  return value ? String(value).trim() : ''
}

function resetForm() {
  Object.assign(form, {
    bikeId: null,
    bikeCode: '',
    modelId: null,
    frameNo: '',
    frameSize: '',
    color: '',
    currentStoreId: null,
    bikeStatus: 1,
    purchaseDate: '',
    purchasePrice: 0,
    mileageKm: 0,
    gpsDeviceNo: '',
    remark: ''
  })
}

function resetModelForm() {
  Object.assign(modelForm, {
    brandName: '',
    seriesName: '',
    modelName: '',
    bikeType: '',
    frameMaterial: '',
    gearSystem: '',
    brakeType: '',
    wheelSize: '700C',
    marketPrice: 0,
    coverImageUrl: '',
    description: '',
    status: 1
  })
}

async function openCreateDialog() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate?.()
}

async function openEditDialog(row) {
  dialogMode.value = 'edit'
  Object.assign(form, {
    bikeId: row.bikeId,
    bikeCode: row.code || '',
    modelId: row.modelId,
    frameNo: row.frameNo || '',
    frameSize: normalizeBikeFrameSize(row.size || ''),
    color: normalizeBikeColor(row.color || ''),
    currentStoreId: row.storeId ?? null,
    bikeStatus: row.statusCode ?? 1,
    purchaseDate: row.purchaseDate || '',
    purchasePrice: row.purchasePrice ?? 0,
    mileageKm: row.mileage ?? 0,
    gpsDeviceNo: row.gpsDeviceNo || '',
    remark: row.remark || ''
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate?.()
}

async function openModelDialog() {
  resetModelForm()
  modelDialogVisible.value = true
  await nextTick()
  modelFormRef.value?.clearValidate?.()
}

async function loadData() {
  loading.value = true
  try {
    const [data, options] = await Promise.all([getAdminVehicles(), getAdminVehicleFormOptions()])
    list.value = data.list || []
    summary.value = data.summary || summary.value
    modelOptions.value = options.models || []
    storeOptions.value = options.stores || []
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
    const payload = {
      bikeCode: normalizeText(form.bikeCode),
      modelId: form.modelId,
      frameNo: normalizeText(form.frameNo),
      frameSize: normalizeBikeFrameSize(form.frameSize),
      color: normalizeBikeColor(form.color),
      currentStoreId: form.currentStoreId,
      bikeStatus: form.bikeStatus,
      purchaseDate: form.purchaseDate || null,
      purchasePrice: form.purchasePrice ?? 0,
      mileageKm: form.mileageKm ?? 0,
      gpsDeviceNo: normalizeText(form.gpsDeviceNo),
      remark: normalizeText(form.remark)
    }

    if (dialogMode.value === 'create') {
      await createAdminVehicle(payload)
      ElMessage.success('车辆已新增')
    } else {
      await updateAdminVehicle(form.bikeId, payload)
      ElMessage.success('车辆信息已更新')
    }

    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleCreateModel() {
  const valid = await modelFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  modelSaving.value = true
  try {
    const savedModel = await createAdminBikeModel({
      brandName: normalizeText(modelForm.brandName),
      seriesName: normalizeText(modelForm.seriesName),
      modelName: normalizeText(modelForm.modelName),
      bikeType: normalizeText(modelForm.bikeType),
      frameMaterial: normalizeText(modelForm.frameMaterial),
      gearSystem: normalizeText(modelForm.gearSystem),
      brakeType: normalizeText(modelForm.brakeType),
      wheelSize: normalizeText(modelForm.wheelSize),
      marketPrice: modelForm.marketPrice ?? 0,
      coverImageUrl: modelForm.coverImageUrl || null,
      description: normalizeText(modelForm.description),
      status: 1
    })

    await loadData()
    form.modelId = savedModel.modelId
    modelDialogVisible.value = false
    ElMessage.success('车型已新增，并自动选中')
  } finally {
    modelSaving.value = false
  }
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
    const imageUrl = await uploadAdminBikeCover(options.file)
    modelForm.coverImageUrl = imageUrl
    options.onSuccess?.({ url: imageUrl })
    ElMessage.success('车型图片上传成功')
  } catch (error) {
    options.onError?.(error)
  } finally {
    coverUploading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除车辆 ${row.code} 吗？`, '删除车辆', {
      type: 'warning'
    })
  } catch {
    return
  }

  await deleteAdminVehicle(row.bikeId)
  ElMessage.success('车辆已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 12px;
}

.model-select-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.model-select-row :deep(.el-select) {
  flex: 1;
}

.cover-uploader {
  width: 100%;
}

.cover-uploader :deep(.el-upload) {
  width: 100%;
}

.cover-preview,
.cover-placeholder {
  width: 100%;
  height: 220px;
  border-radius: 18px;
  border: 1px dashed rgba(15, 23, 42, 0.14);
  background: #f8fafc;
}

.cover-preview {
  display: block;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-secondary);
}

.cover-placeholder small {
  font-size: 12px;
}

@media (max-width: 720px) {
  .model-select-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
