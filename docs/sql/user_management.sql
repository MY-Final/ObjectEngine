-- 用户管理菜单（挂在「后台管理」目录下，排在通用选项集后面）
INSERT INTO custom_menu
  (parent_id, menu_name, menu_type, route_path, icon, sort, status, visible, target, remark, created_at, updated_at)
SELECT id, '用户管理', 'LINK', '/admin/users', 'User', 4, 1, 1, '_self', '系统用户管理', NOW(), NOW()
FROM custom_menu
WHERE menu_name = '后台管理' AND menu_type = 'DIRECTORY'
LIMIT 1;
