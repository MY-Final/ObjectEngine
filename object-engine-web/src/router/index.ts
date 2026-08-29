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
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '首页' },
        },
        {
          path: 'objects',
          name: 'object-list',
          component: () => import('@/views/object/index.vue'),
          meta: { title: '对象管理' },
        },
        {
          path: 'objects/:apiName',
          name: 'object-detail',
          component: () => import('@/views/object/detail.vue'),
          meta: { title: '对象详情' },
        },
        {
          path: 'objects/:apiName/fields',
          name: 'object-fields',
          component: () => import('@/views/object/fields.vue'),
          meta: { title: '字段配置' },
        },
        {
          path: 'custom/:apiName',
          name: 'custom-object',
          component: () => import('@/views/custom/object.vue'),
          meta: { title: '动态对象' },
        },
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
