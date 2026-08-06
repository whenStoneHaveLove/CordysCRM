-- V3.0.0_9: 更新 sys_module 记录：adResourceManagement → adUpstreamAgent
UPDATE sys_module SET module_key = 'adUpstreamAgent' WHERE module_key = 'adResourceManagement';
