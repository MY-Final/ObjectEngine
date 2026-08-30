-- 初始管理员账号：admin / admin123（密码为 BCrypt 摘要，登录后请尽快修改）
INSERT INTO sys_user (username, password, name, email, phone, status, remark)
VALUES ('admin', '$2a$10$2UNS7X0lvgS.4UqBcVH9V.Oc1CLhKK.Wao5uppmFySfeYN3zzizd.', '管理员', NULL, NULL, 1, '初始管理员账号');
