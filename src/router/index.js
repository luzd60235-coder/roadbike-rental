import { createRouter, createWebHistory } from 'vue-router'
import SideLayout from '@/layouts/SideLayout.vue'
import LoginView from '@/views/auth/LoginView.vue'
import AdminProfileView from '@/views/admin/AdminProfileView.vue'
import ModelManagement from '@/views/admin/ModelManagement.vue'
import OrderManagement from '@/views/admin/OrderManagement.vue'
import PriceManagement from '@/views/admin/PriceManagement.vue'
import StaffManagement from '@/views/admin/StaffManagement.vue'
import StatisticsView from '@/views/admin/StatisticsView.vue'
import StoreManagement from '@/views/admin/StoreManagement.vue'
import VehicleManagement from '@/views/admin/VehicleManagement.vue'
import BikeDetail from '@/views/user/BikeDetail.vue'
import BikeList from '@/views/user/BikeList.vue'
import MyFavorites from '@/views/user/MyFavorites.vue'
import MyOrders from '@/views/user/MyOrders.vue'
import StoreDetail from '@/views/user/StoreDetail.vue'
import StoreQuery from '@/views/user/StoreQuery.vue'
import UserHome from '@/views/user/UserHome.vue'
import UserProfileView from '@/views/user/UserProfileView.vue'
import MaintenanceManagement from '@/views/staff/MaintenanceManagement.vue'
import StaffProfileView from '@/views/staff/StaffProfileView.vue'
import SettlementManagement from '@/views/staff/SettlementManagement.vue'
import TradeManagement from '@/views/staff/TradeManagement.vue'
import { getHomePathByRole, getToken, getUser } from '@/utils/auth'
// 前端路由目录
const routes = [
  {
    path: '/',
    redirect: () => {
      const user = getUser()
      return user ? getHomePathByRole(user.role) : '/login'
    }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      public: true,
      title: '登录'
    }
  },
  {
    path: '/admin',
    component: SideLayout,
    redirect: '/admin/vehicles',
    meta: {
      role: 'admin',
      title: '后台管理'
    },
    children: [
      {
        path: 'vehicles',
        name: 'admin-vehicles',
        component: VehicleManagement,
        meta: { role: 'admin', title: '车辆管理' }
      },
      {
        path: 'models',
        name: 'admin-models',
        component: ModelManagement,
        meta: { role: 'admin', title: '车型管理' }
      },
      {
        path: 'orders',
        name: 'admin-orders',
        component: OrderManagement,
        meta: { role: 'admin', title: '订单管理' }
      },
      {
        path: 'pricing',
        name: 'admin-pricing',
        component: PriceManagement,
        meta: { role: 'admin', title: '价格管理' }
      },
      {
        path: 'statistics',
        name: 'admin-statistics',
        component: StatisticsView,
        meta: { role: 'admin', title: '数据统计' }
      },
      {
        path: 'stores',
        name: 'admin-stores',
        component: StoreManagement,
        meta: { role: 'admin', title: '门店管理' }
      },
      {
        path: 'staff-members',
        name: 'admin-staff-members',
        component: StaffManagement,
        meta: { role: 'admin', title: '门店员工管理' }
      },
      {
        path: 'profile',
        name: 'admin-profile',
        component: AdminProfileView,
        meta: { role: 'admin', title: '个人中心' }
      }
    ]
  },
  {
    path: '/user',
    component: SideLayout,
    redirect: '/user/home',
    meta: {
      role: 'user',
      title: '用户端'
    },
    children: [
      {
        path: 'home',
        name: 'user-home',
        component: UserHome,
        meta: { role: 'user', title: '首页' }
      },
      {
        path: 'bikes',
        name: 'user-bikes',
        component: BikeList,
        meta: { role: 'user', title: '车辆列表' }
      },
      {
        path: 'bikes/:id',
        name: 'user-bike-detail',
        component: BikeDetail,
        meta: {
          role: 'user',
          title: '车辆详情',
          activeMenu: '/user/bikes'
        }
      },
      {
        path: 'orders',
        name: 'user-orders',
        component: MyOrders,
        meta: { role: 'user', title: '我的订单' }
      },
      {
        path: 'favorites',
        name: 'user-favorites',
        component: MyFavorites,
        meta: { role: 'user', title: '我的收藏' }
      },
      {
        path: 'stores',
        name: 'user-stores',
        component: StoreQuery,
        meta: { role: 'user', title: '门店查询' }
      },
      {
        path: 'stores/:id',
        name: 'user-store-detail',
        component: StoreDetail,
        meta: {
          role: 'user',
          title: '门店详情',
          activeMenu: '/user/stores'
        }
      },
      {
        path: 'profile',
        name: 'user-profile',
        component: UserProfileView,
        meta: { role: 'user', title: '个人中心' }
      }
    ]
  },
  {
    path: '/staff',
    component: SideLayout,
    redirect: '/staff/trades',
    meta: {
      role: 'staff',
      title: '门店端'
    },
    children: [
      {
        path: 'trades',
        name: 'staff-trades',
        component: TradeManagement,
        meta: { role: 'staff', title: '交易管理' }
      },
      {
        path: 'settlements',
        name: 'staff-settlements',
        component: SettlementManagement,
        meta: { role: 'staff', title: '结算管理' }
      },
      {
        path: 'maintenance',
        name: 'staff-maintenance',
        component: MaintenanceManagement,
        meta: { role: 'staff', title: '维修管理' }
      },
      {
        path: 'profile',
        name: 'staff-profile',
        component: StaffProfileView,
        meta: { role: 'staff', title: '个人中心' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = getToken()
  const user = getUser()

  if (to.meta.public) {
    if (token && user) {
      return getHomePathByRole(user.role)
    }
    return true
  }

  if (!token || !user) {
    return '/login'
  }

  if (to.meta.role && to.meta.role !== user.role) {
    return getHomePathByRole(user.role)
  }

  return true
})

export default router
