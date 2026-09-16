# CordysCRM 项目记忆

## 项目概述
- 飞致云开源 AI CRM（GitHub: 1Panel-dev/CordysCRM），协议 FIT2CLOUD OSS（本质 GPLv3，不可替换 Logo/版权）
- 当前分支：`dev3.0`（基于开源版二次开发）

## 技术栈
- 后端：Spring Boot 3.x + Java 21 + Jetty、MyBatis（自定义 BaseMapper）+ PageHelper、Shiro 2.x (Jakarta)、Redis + Redisson + Spring Session、Flyway、SpringDoc/Swagger 3、Quartz、FastExcel、Auth0 java-jwt
- 前端：Vue 3.5 + TS 5.9、Naive UI 2.44+ (Web) / Vant 4.8+ (Mobile)、Vite 7 (web) / Vite 6 (mobile)、pnpm monorepo、Vue Router 4.5 (Hash)、Pinia 2.3 + persistedstate、Axios 1.7 (CordysAxios)、Tailwind 3.4 + Less、ECharts 6、vue-i18n 9
- 目录：后端 `backend/crm/src/main/java/cn/cordys/`（config/common/crm/ad）；前端 Web `frontend/packages/web/src/views/advertising/`；共享库 `frontend/packages/lib-shared/`

## Git 提交约定
- **必须 `--no-verify`**：husky pre-commit 会跑 vue-tsc/lint-staged，易卡住或报 `Cannot find type definition file for 'vite/client'`
- cmd 回显中文提交信息是 GBK 乱码，**提交内容本身正确 UTF-8**；验证：`git log -1 --format=%B > tmp.txt` 后按 UTF-8 读取
- **脏工作区精准提交**：先 `git status --porcelain` 查暂存区（常遗留 `build_compile.bat`/`tsc_check.bat`/`compile_out.txt`/`eslint_out.txt`/`tsc_out.txt`，用 `git restore --staged <file>` 剔除）→ 只 `git add` 本次相关文件 → `git diff --cached --stat` 复核 → commit。用户常累积大量本地改动，勿误提交无关文件

## 核心业务流程
- 订单生命周期：草稿(0) → 已提交(10) → 审批通过(20) → 执行中(30/40) → 执行完成(50) → 已归档(60)；驳回(15)、作废(70)、强制归档(80)
- 合同用印状态：未申请(0) → 审批中(10) → 已用印(20) → 归档审批中(40) → 已归档(60)；已驳回(30)、归档驳回(50)
- 合同类型：10框架 20单笔 30服务 40其他；合同方向：10上游（我方甲方） 20下游（我方乙方）

## 开发原则
- 改实体类前**先看 DDL**：不是所有 `ad_*` 表都有 BaseModel 全部字段
- 局部问题局部修，勿因 INSERT 报错就去掉 `extends BaseModel`
- 新增 Flyway 迁移前检查版本号冲突（版本号全局唯一，不区分 ddl/dml 目录）
- 附件路径 `/opt/cordys/data/files`：`tmp/` 临时、`pic/{orgId}/` 正式；`TaskCleanupJob` 每天 3 点触发

## BaseModel 与 BaseMapper（易错）
```java
id (String) createUser (String) updateUser (String)   // DDL 均须 VARCHAR(32)
createTime (Long) updateTime (Long)                    // DDL 须 BIGINT（非 DATETIME）
```
- `BaseMapper.insert` 是**非选择性 INSERT**（插全部字段），表结构必须覆盖实体所有字段
- 项目**不是** MyBatis-Plus：Lambda 查询是 `selectListByLambda(LambdaQueryWrapper)`；全量是 `selectAll(orderBy)`（blank 时主键 DESC）；按主键更新 `updateById`
- **巨坑**：`BaseMapper.select(E criteria)` 把 int 默认值 0 当 WHERE 条件（`status/paymentDone/...`）→ 全表扫描严禁 `new E() + setDeleted(0)`。补救：XML 自定义方法 / `selectAll` + stream 过滤 / `selectListByLambda`。参考模板 `crm/.../ad/order/mapper/ExtAdOrderMapper.xml` 的 `selectAllNonDeleted`
- Flyway `validate-on-migrate=false`（`backend/app/src/main/resources/commons.properties`）：改已执行迁移不报 checksum 错但不重跑，修结构须加更高版本号迁移

## 表结构速查（ad_* 不含完整 BaseModel）
- `ad_order_log`：无 deleted / create_user / update_user / update_time
- `ad_order_contract`：无 create_user / update_user / update_time（有 deleted、create_time）
- `ad_order_downstream_media`：同上，实体类不继承 BaseModel
- 新建 `ad_*` 子表模板：`V3.0.0_13__ad_receipt_payment.sql`

## 逻辑删除 + 唯一键的通用坑
- 部分表唯一键**不含 `deleted`**：「先逻辑删旧行 → insert 新行」第二次会 `Duplicate entry`
- 正确写法：查全部（含已删）→ 目标集合外置 `deleted=1` → 已存在行复用（`deleted` 复位后 update）→ 仅真不存在才 insert；入参先 `LinkedHashSet` 去空去重
- 坑位：`ad_order_contract.uk_ad_oc`（订单端 `AdOrderService.syncOrderContract` 正确；合同端 `AdContractService` 已于 2026-09-16 修复）、`ad_order_downstream_media.uk_ad_odm`（已处理）、`ad_payout_invoice`（走 update + 物理删除，安全）
- 对称查询：`ExtAdOrderContractMapper.selectAllByOrderId / selectAllByContractId`（均不过滤 deleted）

## 广告模块关键约定
- 合同-订单关联统一走 `ad_order_contract` 中间表；订单端 UI 目前仅单选
- 订单编辑回填：`loadForEdit` 回填 `orderType` 触发 watch，需 `isRestoringFromDetail` 标志避免清空 `contractId`
- 合同附件用印/双盖保存时调 `AttachmentService.processTemp` 转存正式目录
- 订单页合同下拉只列 `sealStatus=60`（已归档/双盖完成）
- **操作日志 i18n**：后端 `module`/`action` 为英文枚举，须同步三处 —— `frontend/packages/web/src/config/adLog.ts`（value+label）、`views/advertising/locale/zh-CN.ts`、`en-US.ts`
  - action 全集：CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE/VOID/FORCE_ARCHIVE/FINANCIAL_PRE_ACTION/CONFIRM_EXECUTE/COMPLETE_EXECUTE/DISABLE/APPLY/CLOSE/RECEIVE/PAY_MEDIA_PREPAY/PAY_MEDIA_POSTPAY/RED_INVOICE_CLEAR/CANCEL/CHANGE_REFUND/CHANGE_BAD_DEBT/SUBMIT_ARCHIVE/APPROVE_ARCHIVE/REJECT_ARCHIVE/OVERDUE_AUTO_50_TO_80
  - module：AD_ORDER / AD_ORDER_CHANGE / AD_CONTRACT / AD_CUSTOMER / AD_BUSINESS_ENTITY / AD_DOWNSTREAM_MEDIA / AD_UPSTREAM_AGENT / AD_PAYMENT / AD_SEAL / AD_SUPPORT / AD_DICT

## 附件上传/预览/下载
- 创建表单内先暂存附件（pendingFiles），创建订单后用 orderId 批量上传；提交时前后端均校验盖章排期(type=10)、邮件截图(type=20)必传
- 预览 `Content-Disposition: inline`、下载 `attachment`；Shiro 白名单 `/attachment/download/**`、`/attachment/preview/**`
- 前端预览路径 `/attachment/preview/{id}?userId=xxx`（userId 后端未实际使用，仅保持一致）
- **能否内联预览只取决于响应 Content-Type**：pdf / image/* → inline，否则 octet-stream 强制下载。判断统一在 `AttachmentService.resolveContentType`；"预览变下载"先查这里
- **两类附件存储不同**：合同附件走 `processTemp` 转正式目录并写 `sys_attachment`；订单附件（`AdOrderAttachmentService.upload`）**只 `uploadTemp`，永久留在 `tmp/` 且无 attachment 记录** → 有被每日清理风险

## Naive UI 避坑
- 表格 `scroll-x` 必须 ≥ 所有列宽之和（含 fixed）+ 50~100 余量
- `n-modal preset="card"` 不自动渲染底部按钮，须手动 `<template #footer>`
- 未开 autoImport，组件须在 `script setup` 显式 `import { NInput } from 'naive-ui'`
- `n-upload` 本版本 es/upload **无 `slots.file` 插槽**；自定义文件项须在 `:show-file-list="false"` 下自渲染
- `custom-request` 里改 `opts.file.id` 改不到列表对象（`createSettledFileInfo` 浅拷贝），须按 `f.file === rawFile` 回写真实列表项

## ESLint simple-import-sort
- 以 `packages/web/eslint.config.cjs` 为准；分组：node 依赖 → `@/assets` → `@lib/shared/.*` → 组件 → `@/` 公共模块 → `@/models`/`@/enums` → `^type`
- 修复：`pnpm exec eslint --fix --config packages/web/eslint.config.cjs <file>`（勿用 npx）

## 广告订单 Excel 导出（已实现并提交）
- 同步流式下载：前端 `exportAdOrder`（`CDR.post` + `responseType:'blob'` + `isReturnNativeResponse:true`）→ 后端 `AdOrderController.export` 用 `EasyExcel.write(response.getOutputStream())` 回写，`Content-disposition: attachment;filename=...`
- 权限：`@CsPermission(PermissionConstants.AD_ORDER_EXPORT)`，三方权限一致
- 导出列：前端从 `columns`（排除 action）取 `{key,title}` 传 `AdOrderPageRequest.headList`；后端按 title 在 `HEAD_VALUE_MAPPER` 匹配取值器
- 关键坑：
  1. 后端 mapper key（中文标题）必须**严格等于**前端 columns 的 `title`（i18n 结果），否则该列空。当前前端译为「应付 / 投放起始 / 投放结束」（短名）
  2. **勿用原生 `fetch`**：鉴权靠请求头 `X-AUTH-TOKEN` + `CSRF-TOKEN`（非 cookie），原生 fetch 会 401，必须用 `CDR`
  3. 勿用 `EasyExcelExporter.exportByCustomWriteHandler(..., null)`（NPE）；列宽/样式用 `registerWriteHandler` + 自定义 `AbstractHeadColumnWidthStyleStrategy` / `HorizontalCellStyleStrategy`
  4. EasyExcel 包名是 **`cn.idev.excel`**（fork，非 `com.alibaba.excel`）
  5. 导出方法 return `void` 直接写 response，避免被 `ResultResponseBodyAdvice` 包装
- 相关文件：`AdOrderController.java`、`AdOrderService.exportList`、`AdOrderPageRequest.headList`、前端 `adOrder.ts`/`requrls/adOrder.ts`/`order/index.vue`

## 广告订单「复制」功能（2026-09-16，进行中）
- 需求：列表「详情」列改「操作」，详情后加「复制」按钮（权限 `AD_ORDER:COPY`）；弹窗勾选要复制的字段（默认勾选：业务主体、订单类型、客户ID、上游代理、下游客户、返点方式、收款方式、账期天数）；点复制生成**草稿**订单，名称加 `-复制` 后缀，未勾选字段置空；放宽 ad_order 非空约束，前端校验保留
- 权限三处已一致：`PermissionConstants.AD_ORDER_COPY`、`permission.json` 的 `AD_ORDER:COPY`、i18n `permission.copy`（zh_CN=复制 / en_US=Copy）
- 已新增：`AdOrderCopyRequest.java`、`ddl/V3.0.0_40__ad_order_copy_nullable.sql`、`dml/V3.0.0_41__ad_order_copy_permission.sql`；`AdOrderController.copy`（`@PostMapping("/copy")`）；`AdOrderService.copy`
- 编号坑：业务主体为 NULL 时 `countTodayOrders`（`business_entity_id = ?`）恒不成立 → 流水永远 001。新增 `countTodayOrdersWithoutEntity`（`IS NULL`）并接入
- 待办：前端「操作」列改造 + 复制弹窗、API 层 `copy` 封装、Service `copy()` 完整字段映射与下游媒体同步
