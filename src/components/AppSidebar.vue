<template>
  <el-menu
    :default-active="activePath"
    :collapse="collapse"
    :collapse-transition="false"
    router
    class="side-menu"
  >
    <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
      <el-icon>
        <component :is="item.icon" />
      </el-icon>
      <span>{{ item.title }}</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
defineProps({
  menus: {
    type: Array,
    default: () => []
  },
  collapse: {
    type: Boolean,
    default: false
  },
  activePath: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.side-menu {
  border-right: 0;
  background: transparent;
}

.side-menu :deep(.el-menu-item) {
  height: 50px;
  margin: 8px 10px;
  border-radius: 14px;
  color: rgba(255, 255, 255, 0.72);
}

.side-menu :deep(.el-menu-item:hover) {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.08);
}

.side-menu :deep(.el-menu-item.is-active) {
  color: #ffffff;
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.26), rgba(15, 118, 110, 0.5));
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.14);
}
</style>
<!--前端可复用的组件目录-->

