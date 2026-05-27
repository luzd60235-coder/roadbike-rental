<template>
  <div class="dashboard-shell">
    <aside :class="['dashboard-aside', { collapse }]">
      <div class="brand-panel">
        <div class="brand-badge">{{ roleDisplay.short }}</div>
        <div v-show="!collapse">
          <div class="brand-title">{{ roleDisplay.title }}</div>
          <div class="brand-desc">{{ roleDisplay.description }}</div>
        </div>
      </div>

      <app-sidebar :menus="menus" :collapse="collapse" :active-path="activePath" />
    </aside>

    <div class="dashboard-main">
      <header class="dashboard-header">
        <div class="header-left">
          <el-button circle plain @click="collapse = !collapse">
            <el-icon>
              <component :is="collapse ? Expand : Fold" />
            </el-icon>
          </el-button>

          <div>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
                {{ item.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
            <div class="header-title">{{ currentTitle }}</div>
          </div>
        </div>

        <div class="header-right">
          <div class="user-chip">
            <div class="user-avatar">{{ currentUser.avatar || roleDisplay.short }}</div>
            <div>
              <div class="user-name">{{ currentUser.name || '访客' }}</div>
              <div class="user-motto">{{ currentUser.storeName || currentUser.motto }}</div>
            </div>
          </div>

          <el-button plain @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </header>

      <main class="dashboard-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Expand, Fold, SwitchButton } from '@element-plus/icons-vue'
import AppSidebar from '@/components/AppSidebar.vue'
import { roleMenus, roleMetaMap } from '@/constants/navigation'
import { clearAuthSession, getUser, getUserChangeEventName } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const collapse = ref(false)

const currentUser = ref(getUser() || {})
const role = computed(() => route.meta.role || currentUser.value.role || 'user')
const roleDisplay = computed(() => roleMetaMap[role.value] || roleMetaMap.user)
const menus = computed(() => roleMenus[role.value] || [])
const activePath = computed(() => route.meta.activeMenu || route.path)
const currentTitle = computed(() => route.meta.title || roleDisplay.value.title)
const breadcrumbs = computed(() =>
  route.matched
    .filter((item) => item.meta?.title)
    .map((item) => ({
      path: item.path,
      title: item.meta.title
    }))
)

function syncCurrentUser() {
  currentUser.value = getUser() || {}
}

onMounted(() => {
  if (typeof window === 'undefined') {
    return
  }
  window.addEventListener('storage', syncCurrentUser)
  window.addEventListener(getUserChangeEventName(), syncCurrentUser)
})

onBeforeUnmount(() => {
  if (typeof window === 'undefined') {
    return
  }
  window.removeEventListener('storage', syncCurrentUser)
  window.removeEventListener(getUserChangeEventName(), syncCurrentUser)
})

function handleLogout() {
  clearAuthSession()
  router.replace('/login')
}
</script>

<style scoped>
.dashboard-shell {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  min-height: 100vh;
}

.dashboard-aside {
  display: flex;
  flex-direction: column;
  width: 260px;
  padding: 22px 16px;
  color: #f8fafc;
  background: linear-gradient(180deg, #0f172a 0%, #10283d 45%, #0f766e 100%);
  transition: width 0.24s ease;
}

.dashboard-aside.collapse {
  width: 94px;
}

.brand-panel {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 12px 20px;
}

.brand-badge {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.9), rgba(15, 118, 110, 0.92));
  box-shadow: 0 10px 24px rgba(56, 189, 248, 0.22);
}

.brand-title {
  font-size: 16px;
  font-weight: 700;
}

.brand-desc {
  margin-top: 6px;
  color: rgba(248, 250, 252, 0.72);
  font-size: 12px;
  line-height: 1.5;
}

.dashboard-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 20px;
}

.dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 22px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 24px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(12px);
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-title {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px 8px 8px;
  background: rgba(15, 118, 110, 0.08);
  border-radius: 18px;
}

.user-avatar {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  color: #ffffff;
  font-weight: 700;
  background: linear-gradient(135deg, #0f766e, #1d4ed8);
}

.user-name {
  font-weight: 700;
}

.user-motto {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 12px;
}

.dashboard-content {
  padding-top: 18px;
}

@media (max-width: 1100px) {
  .dashboard-shell {
    grid-template-columns: 1fr;
  }

  .dashboard-aside,
  .dashboard-aside.collapse {
    width: 100%;
    padding: 16px 12px;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-left,
  .header-right {
    flex-wrap: wrap;
  }

  .dashboard-main {
    padding: 12px;
  }
}
</style>
<!-- 系统后台布局，包括左侧菜单、顶部栏、内容区域 -->

