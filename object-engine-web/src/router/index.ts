import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: DefaultLayout,
      redirect: '/dashboard',
      children: [
        // —— 前台区：/dashboard、/custom/*，侧边栏显示前台菜单 ——
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '首页' },
        },
        {
          path: 'custom/:apiName',
          name: 'custom-object',
          component: () => import('@/views/custom/object.vue'),
          meta: { title: '动态对象' },
        },
        // —— 后台区：/admin/*，侧边栏只显示后台菜单 ——
        {
          path: 'admin/objects',
          name: 'object-list',
          component: () => import('@/views/object/index.vue'),
          meta: { title: '对象管理' },
        },
        {
          path: 'admin/objects/:apiName',
          name: 'object-detail',
          component: () => import('@/views/object/detail.vue'),
          meta: { title: '对象详情' },
        },
        {
          path: 'admin/objects/:apiName/fields',
          name: 'object-fields',
          component: () => import('@/views/object/fields.vue'),
          meta: { title: '字段配置' },
        },
        {
          path: 'admin/objects/:apiName/layout',
          name: 'object-layout',
          component: () => import('@/views/object/layout.vue'),
          meta: { title: '布局配置' },
        },
        // 旧地址兼容重定向（书签 / 未更新的菜单数据），redirect 字符串会自动带上路由参数
        { path: 'objects', redirect: '/admin/objects' },
        { path: 'objects/:apiName', redirect: '/admin/objects/:apiName' },
        { path: 'objects/:apiName/fields', redirect: '/admin/objects/:apiName/fields' },
        { path: 'objects/:apiName/layout', redirect: '/admin/objects/:apiName/layout' },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.afterEach((to) => {
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} - Object Engine` : 'Object Engine'
})

export default router
