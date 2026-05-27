<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1 class="page-title">车型管理</h1>
        <p class="page-subtitle">集中维护车型资料、封面图片和展示配置，支持新增、编辑和删除。</p>
      </div>
      <div class="header-actions">
        <el-button plain @click="loadData">刷新列表</el-button>
        <el-button type="primary" @click="openCreateDialog">新增车型</el-button>
      </div>
    </section>

    <section class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">车型总数</div>
        <div class="metric-value">{{ summary.total }}</div>
        <div class="metric-trend">当前已维护的车型数量</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">启用车型</div>
        <div class="metric-value">{{ summary.active }}</div>
        <div class="metric-trend">可用于新增车辆和配置价格</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">已配图片</div>
        <div class="metric-value">{{ summary.withImage }}</div>
        <div class="metric-trend">已上传车型封面的数量</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">已配价格</div>
        <div class="metric-value">{{ summary.withPricing }}</div>
        <div class="metric-trend">已存在租赁价格策略的车型</div>
      </article>
    </section>

    <section class="toolbar-card">
      <div class="filters-row">
        <el-input
          v-model="keyword"
          placeholder="搜索品牌 / 系列 / 车型名称 / 类型"
          clearable
          style="width: 320px"
        />
        <el-select v-model="status" placeholder="车型状态" clearable style="width: 180px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>
    </section>

    <section class="content-card">
      <el-table :data="filteredList" v-loading="loading" stripe>
        <el-table-column label="封面图" min-width="140">
          <template #default="{ row }">
            <img :src="resolveBikeImage(row)" :alt="row.bikeName" class="model-cover" />
          </template>
        </el-table-column>

        <el-table-column label="车型信息" min-width="260">
          <template #default="{ row }">
            <div>{{ row.bikeName }}</div>
            <div class="muted">{{ row.seriesName || '未填写系列' }} / {{ row.bikeType || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="配置摘要" min-width="240">
          <template #default="{ row }">
            <div>{{ row.frameMaterial || '未填写材质' }}</div>
            <div class="muted">{{ row.gearSystem || '未填写变速系统' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="市场价 / 日租价" min-width="160">
          <template #default="{ row }">
            <div>¥ {{ row.marketPrice || 0 }}</div>
            <div class="muted">日租 ¥ {{ row.price || 0 }}</div>
          </template>
        </el-table-column>

        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.modelStatus === 1 ? 'success' : 'info'">
              {{ row.modelStatus === 1 ? '启用' : '停用' }}
            </el-tag>
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
      :title="dialogMode === 'create' ? '新增车型' : '编辑车型'"
      width="820px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="section-grid cols-2">
          <div>
            <el-form-item label="品牌" prop="brandName">
              <el-input v-model="form.brandName" placeholder="例如 Specialized" />
            </el-form-item>

            <el-form-item label="系列">
              <el-input v-model="form.seriesName" placeholder="例如 Tarmac" />
            </el-form-item>

            <el-form-item label="车型名称" prop="modelName">
              <el-input v-model="form.modelName" placeholder="例如 SL8 Comp" />
            </el-form-item>

            <el-form-item label="车辆类型" prop="bikeType">
              <el-select
                v-model="form.bikeType"
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
              <el-input v-model="form.frameMaterial" placeholder="例如 碳纤维" />
            </el-form-item>

            <el-form-item label="变速系统">
              <el-input v-model="form.gearSystem" placeholder="例如 Shimano 105 Di2 24 速" />
            </el-form-item>

            <el-form-item label="制动类型">
              <el-input v-model="form.brakeType" placeholder="例如 液压碟刹" />
            </el-form-item>
          </div>

          <div>
            <el-form-item label="轮组规格">
              <el-input v-model="form.wheelSize" placeholder="例如 700C" />
            </el-form-item>

            <el-form-item label="整车重量（kg）">
              <el-input-number v-model="form.bikeWeight" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>

            <el-form-item label="市场价格">
              <el-input-number v-model="form.marketPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>

            <el-form-item label="车型状态" prop="status">
              <el-select v-model="form.status" placeholder="选择状态" style="width: 100%">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
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
                  v-if="form.coverImageUrl"
                  :src="form.coverImageUrl"
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
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="可填写车型亮点、适用场景和展示说明"
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
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BIKE_TYPE_OPTIONS, appendOptionIfMissing } from '@/constants/adminFormOptions'
import {
  createAdminBikeModel,
  deleteAdminBikeModel,
  getAdminVehicleModels,
  updateAdminBikeModel,
  uploadAdminBikeCover
} from '@/api/admin'
import { resolveBikeImage } from '@/utils/media'

const loading = ref(false)
const saving = ref(false)
const coverUploading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref()
const keyword = ref('')
const status = ref(null)
const list = ref([])

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const form = reactive({
  modelId: null,
  brandName: '',
  seriesName: '',
  modelName: '',
  bikeType: '',
  frameMaterial: '',
  gearSystem: '',
  brakeType: '',
  wheelSize: '700C',
  bikeWeight: null,
  marketPrice: 0,
  coverImageUrl: '',
  description: '',
  status: 1
})

const rules = {
  brandName: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入车型名称', trigger: 'blur' }],
  bikeType: [{ required: true, message: '请选择或输入车辆类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const bikeTypeOptions = computed(() => {
  let options = appendOptionIfMissing(BIKE_TYPE_OPTIONS, form.bikeType)

  for (const item of list.value) {
    options = appendOptionIfMissing(options, item.bikeType)
  }

  return options
})

const filteredList = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()

  return list.value.filter((item) => {
    const searchableText = [
      item.brandName,
      item.seriesName,
      item.modelName,
      item.bikeType,
      item.frameMaterial
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()

    const matchKeyword = !normalizedKeyword || searchableText.includes(normalizedKeyword)
    const matchStatus = status.value == null || item.modelStatus === status.value
    return matchKeyword && matchStatus
  })
})

const summary = computed(() => ({
  total: list.value.length,
  active: list.value.filter((item) => item.modelStatus === 1).length,
  withImage: list.value.filter((item) => !!item.coverImageUrl).length,
  withPricing: list.value.filter((item) => Number(item.price) > 0).length
}))

function normalizeText(value) {
  return value ? String(value).trim() : ''
}

function resetForm() {
  Object.assign(form, {
    modelId: null,
    brandName: '',
    seriesName: '',
    modelName: '',
    bikeType: '',
    frameMaterial: '',
    gearSystem: '',
    brakeType: '',
    wheelSize: '700C',
    bikeWeight: null,
    marketPrice: 0,
    coverImageUrl: '',
    description: '',
    status: 1
  })
}

async function loadData() {
  loading.value = true
  try {
    list.value = await getAdminVehicleModels()
  } finally {
    loading.value = false
  }
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
    modelId: row.modelId,
    brandName: row.brandName || '',
    seriesName: row.seriesName || '',
    modelName: row.modelName || '',
    bikeType: row.bikeType || '',
    frameMaterial: row.frameMaterial || '',
    gearSystem: row.gearSystem || '',
    brakeType: row.brakeType || '',
    wheelSize: row.wheelSize || '700C',
    bikeWeight: row.bikeWeight ?? null,
    marketPrice: row.marketPrice ?? 0,
    coverImageUrl: row.coverImageUrl || '',
    description: row.description || '',
    status: row.modelStatus
  })
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate?.()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload = {
      brandName: normalizeText(form.brandName),
      seriesName: normalizeText(form.seriesName),
      modelName: normalizeText(form.modelName),
      bikeType: normalizeText(form.bikeType),
      frameMaterial: normalizeText(form.frameMaterial),
      gearSystem: normalizeText(form.gearSystem),
      brakeType: normalizeText(form.brakeType),
      wheelSize: normalizeText(form.wheelSize),
      bikeWeight: form.bikeWeight,
      marketPrice: form.marketPrice ?? 0,
      coverImageUrl: form.coverImageUrl || null,
      description: normalizeText(form.description),
      status: form.status
    }

    if (dialogMode.value === 'create') {
      await createAdminBikeModel(payload)
      ElMessage.success('车型已新增')
    } else {
      await updateAdminBikeModel(form.modelId, payload)
      ElMessage.success('车型已更新')
    }

    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除车型 ${row.bikeName} 吗？`, '删除车型', {
      type: 'warning'
    })
  } catch {
    return
  }

  await deleteAdminBikeModel(row.modelId)
  ElMessage.success('车型已删除')
  await loadData()
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
    form.coverImageUrl = imageUrl
    options.onSuccess?.({ url: imageUrl })
    ElMessage.success('车型图片上传成功')
  } catch (error) {
    options.onError?.(error)
  } finally {
    coverUploading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 12px;
}

.model-cover {
  width: 108px;
  height: 72px;
  display: block;
  border-radius: 16px;
  object-fit: cover;
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.08);
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

</style>
