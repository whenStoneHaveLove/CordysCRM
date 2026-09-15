-- 存量数据迁移：把 ad_contract.file_url（用印附件, type=10）迁到 ad_contract_attachment
INSERT INTO ad_contract_attachment (id, contract_id, type, file_url, file_name, organization_id, create_time, create_user, update_time, update_user, deleted)
SELECT REPLACE(UUID(), '-', '') AS id,
       c.id AS contract_id,
       10 AS type,
       c.file_url AS file_url,
       COALESCE(a.name, c.file_url) AS file_name,
       c.organization_id,
       c.create_time,
       c.create_user,
       c.update_time,
       c.update_user,
       0 AS deleted
FROM ad_contract c
LEFT JOIN sys_attachment a ON a.id = c.file_url
WHERE c.deleted = 0
  AND c.file_url IS NOT NULL
  AND c.file_url <> '';

-- 存量数据迁移：把 ad_contract.double_seal_file_url（双盖附件, type=20）迁到 ad_contract_attachment
INSERT INTO ad_contract_attachment (id, contract_id, type, file_url, file_name, organization_id, create_time, create_user, update_time, update_user, deleted)
SELECT REPLACE(UUID(), '-', '') AS id,
       c.id AS contract_id,
       20 AS type,
       c.double_seal_file_url AS file_url,
       COALESCE(a.name, c.double_seal_file_url) AS file_name,
       c.organization_id,
       c.create_time,
       c.create_user,
       c.update_time,
       c.update_user,
       0 AS deleted
FROM ad_contract c
LEFT JOIN sys_attachment a ON a.id = c.double_seal_file_url
WHERE c.deleted = 0
  AND c.double_seal_file_url IS NOT NULL
  AND c.double_seal_file_url <> '';
