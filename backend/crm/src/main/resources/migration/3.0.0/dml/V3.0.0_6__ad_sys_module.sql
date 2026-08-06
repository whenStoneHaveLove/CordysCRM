-- 广告下单系统模块注册到 sys_module（V3.1）
-- 使广告模块在"模块管理"页面可见、可启停

INSERT IGNORE INTO sys_module (id, organization_id, module_key, enable, pos, create_user, create_time, update_user, update_time)
SELECT UUID_SHORT(), '100001', 'adWorkbench',          true, 11, 'admin', UNIX_TIMESTAMP()*1000, 'admin', UNIX_TIMESTAMP()*1000
WHERE NOT EXISTS (SELECT 1 FROM sys_module WHERE organization_id = '100001' AND module_key = 'adWorkbench');

INSERT IGNORE INTO sys_module (id, organization_id, module_key, enable, pos, create_user, create_time, update_user, update_time)
SELECT UUID_SHORT(), '100001', 'adOrderManagement',     true, 12, 'admin', UNIX_TIMESTAMP()*1000, 'admin', UNIX_TIMESTAMP()*1000
WHERE NOT EXISTS (SELECT 1 FROM sys_module WHERE organization_id = '100001' AND module_key = 'adOrderManagement');

INSERT IGNORE INTO sys_module (id, organization_id, module_key, enable, pos, create_user, create_time, update_user, update_time)
SELECT UUID_SHORT(), '100001', 'adContractManagement',  true, 13, 'admin', UNIX_TIMESTAMP()*1000, 'admin', UNIX_TIMESTAMP()*1000
WHERE NOT EXISTS (SELECT 1 FROM sys_module WHERE organization_id = '100001' AND module_key = 'adContractManagement');

INSERT IGNORE INTO sys_module (id, organization_id, module_key, enable, pos, create_user, create_time, update_user, update_time)
SELECT UUID_SHORT(), '100001', 'adResourceManagement',  true, 14, 'admin', UNIX_TIMESTAMP()*1000, 'admin', UNIX_TIMESTAMP()*1000
WHERE NOT EXISTS (SELECT 1 FROM sys_module WHERE organization_id = '100001' AND module_key = 'adResourceManagement');

INSERT IGNORE INTO sys_module (id, organization_id, module_key, enable, pos, create_user, create_time, update_user, update_time)
SELECT UUID_SHORT(), '100001', 'adApproval',            true, 15, 'admin', UNIX_TIMESTAMP()*1000, 'admin', UNIX_TIMESTAMP()*1000
WHERE NOT EXISTS (SELECT 1 FROM sys_module WHERE organization_id = '100001' AND module_key = 'adApproval');

INSERT IGNORE INTO sys_module (id, organization_id, module_key, enable, pos, create_user, create_time, update_user, update_time)
SELECT UUID_SHORT(), '100001', 'adReport',              true, 16, 'admin', UNIX_TIMESTAMP()*1000, 'admin', UNIX_TIMESTAMP()*1000
WHERE NOT EXISTS (SELECT 1 FROM sys_module WHERE organization_id = '100001' AND module_key = 'adReport');

INSERT IGNORE INTO sys_module (id, organization_id, module_key, enable, pos, create_user, create_time, update_user, update_time)
SELECT UUID_SHORT(), '100001', 'adSystem',              true, 17, 'admin', UNIX_TIMESTAMP()*1000, 'admin', UNIX_TIMESTAMP()*1000
WHERE NOT EXISTS (SELECT 1 FROM sys_module WHERE organization_id = '100001' AND module_key = 'adSystem');
