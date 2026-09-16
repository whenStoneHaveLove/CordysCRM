# CordysCRM 项目记忆

## 项目概述
- 飞致云开源 AI CRM（1Panel-dev/CordysCRM，FIT2CLOUD OSS ≈ GPLv3，不可替换 Logo/版权）；分支 `dev3.0`（二次开发）
- 后端：Spring Boot 3 + Java 21 + Jetty、MyBatis 自定义 BaseMapper + PageHelper、Shiro 2(Jakarta)、Redis + Redisson + Spring Session、Flyway、SpringDoc、Quartz、FastExcel
- 前端：Vue 3.5 + TS、Naive UI(web)/Vant(mobile)、Vite、pnpm monorepo、Pinia、Axios(CordysAxios)、Tailwind 3.4 + Less、ECharts、vue-i18n
- 路径：后端 `backend/crm/src/main/java/cn/cordys/`（config/common/crm/ad）、framework `backend/framework/`；前端 `frontend/packages/web/src/views/advertising/`；共享 `frontend/packages/lib-shared/`

## Git 提交约定（精简）
- **必须 `--no-verify`**（husky 跑 vue-tsc/lint-staged 易卡或报 vite/client 类型错）
- 脏工作区精准提交：`git status --porcelain` → 剔除遗留临时文件（`build_compile.bat`/`*_out.txt`）→ 只 add 相关文件 → `git diff --cached --stat` 复核
- cmd 回显中文是 GBK 乱码，内容本身 UTF-8 正确

## 核心业务
- 订单：草稿0 → 已提交10 → 审批通过20 → 执行中30/40 → 执行完成50 → 归档60；驳回15、作废70、强制归档80
- 合同用印：未申请0 → 审批中10 → 已用印20 → 归档审批中40 → 已归档60；驳回30、归档驳回50
- 合同类型 10框架/20单笔/30服务/40其他；方向 10上游/20下游

## 开发原则 & 存储
- 改实体前**先看 DDL**（ad_* 不一定有完整 BaseModel 字段）；勿因 INSERT 报错去掉 `extends BaseModel`
- Flyway 版本号全局唯一（不区分 ddl/dml 目录）；`validate-on-migrate=false`（改已执行迁移不重跑）
- 附件目录 `/opt/cordys/data/files`：`tmp/` 临时、`pic/{orgId}/` 正式；`TaskCleanupJob` 每天 3 点清理

## BaseModel / BaseMapper 易错
- `id/createUser/updateUser` VARCHAR(32)；`createTime/updateTime` BIGINT（非 DATETIME）
- `insert` 是**全字段 INSERT**；非 MyBatis-Plus：Lambda 用 `selectListByLambda`、全量 `selectAll`、按主键 `updateById`
- **巨坑**：`select(E criteria)` 把 int 默认值 0 当条件（`status/deleted/...`）→ 严禁 `new E()+setDeleted(0)`；改用 XML 自定义 / `selectAll`+stream / Lambda

## 表结构速查（ad_* 缺字段）
- `ad_order_log`：无 deleted/create_user/update_user/update_time
- `ad_order_contract`、`ad_order_downstream_media`：无 create_user/update_user/update_time（有 deleted、create_time）
- 新建子表模板：`V3.0.0_13__ad_receipt_payment.sql`

## 逻辑删除 + 唯一键通用坑
- 唯一键不含 `deleted` 时「逻辑删 → 再插」会 Duplicate；正确：查全部（含已删）→ 目标外置 deleted=1 → 已存在复用 → 仅新增才 insert
- 已修：订单端 `AdOrderService.syncOrderContract`、`AdContractService`、`ad_order_downstream_media`；`ad_payout_invoice` 走 update+物理删（安全）

## 广告模块约定
- 合同-订单关联走 `ad_order_contract`；订单页合同下拉只列 `sealStatus=60`
- 订单编辑回填 `loadForEdit`：用 `isRestoringFromDetail` 标志避免 watch 清空 `contractId`
- **操作日志 i18n**：后端英文 module/action 须同步三处 `config/adLog.ts`、`zh-CN.ts`、`en-US.ts`

## 附件上传 / 预览 / 下载
- 创建表单内暂存 pendingFiles，创建订单后按 orderId 批量上传；提交时前后端校验排期(10)+邮件截图(20)（有 eml 则豁免，见下）
- 预览 `inline` / 下载 `attachment`；**能否内联只取决于 Content-Type**（pdf、image/* 才行），判断在 `AttachmentService.resolveContentType`
- 两类存储：合同附件 `processTemp` 转正式目录并写 `sys_attachment`；订单附件只 `uploadTemp`，长期留在 `tmp/` 且无附件记录（有被清理风险）
- 前端预览 URL：`/attachment/preview/{id}?userId=xxx`（userId 后端不用）
- **预览 401 排查链**：`/attachment/**` 与 `/pic/**` 走 `authf`(`FileAccessAuthFilter`)，只认 **Cookie `F_A_TOKEN`**（登录时 `FileAccessTokenUtils.setAccessCookie` 写入，AES(sessionId, `cordys.secret.key`)），且 `SessionUtils.sessionExists(sessionId)` 必须为真（查 **Redis** session）。→ 401 = 未重新登录 / Redis session 没了 / secret 变了 / 无痕窗口直接粘 URL。与业务代码无关

## Naive UI 避坑
- `scroll-x`（写死列宽）须 ≥ 各列宽之和 + 50~100；动态求和用 `total + 100`
- "右侧空白列"先怀疑**操作列过宽**；三按钮同行需 `NSpace` 显式 `wrap:false`
- `n-modal preset="card"` 需手动 `<template #footer>`；未开 autoImport，组件须显式 import
- `n-upload` 无 `slots.file`，自定义列表用 `:show-file-list="false"` 自渲染；`custom-request` 改 `opts.file.id` 无效，须按 `f.file === rawFile` 回写真实项

## ESLint
- 以 `packages/web/eslint.config.cjs` 为准（simple-import-sort 分组：node → @/assets → @lib/shared → 组件 → @/ → @/models、@/enums → type）
- 修复：`pnpm exec eslint --fix --config packages/web/eslint.config.cjs <file>`（勿用 npx）

## 广告订单 Excel 导出（已实现）
- 前端 `exportAdOrder`（`CDR.post` + blob + `isReturnNativeResponse`）→ 后端 `AdOrderController.export` 用 `EasyExcel.write(response.getOutputStream())` 直写，方法 return void 避免被 `ResultResponseBodyAdvice` 包装
- 坑：后端中文 mapper key 必须 = 前端 columns 的 `title`；**勿用原生 fetch**（鉴权靠 `X-AUTH-TOKEN`+`CSRF-TOKEN` 头）；EasyExcel 包名 `cn.idev.excel`；勿用 `exportByCustomWriteHandler(..., null)`（NPE）

## 广告订单「复制」（2026-09-16，已提交 8550444fb）
- 列表「操作」列：详情/复制/编辑（`AD_ORDER:COPY`）；弹窗勾选字段 → 生成草稿，名称加 `-复制`
- 后端 `AdOrderCopyRequest` + `AdOrderController.copy` + `AdOrderService.copy`（字段常量 nested class `CopyField`）+ 迁移 V3.0.0_40/41
- 坑：业务主体 NULL 时流水号恒 001（新增 `countTodayOrdersWithoutEntity`）；未勾下游客户须把媒体金额置 0；`refreshColumns` 持久化宽度会覆盖 action 列新值，需特判

## 广告订单附件：eml 邮件记录（2026-09-16 已实现）
- 新增类型 **60=邮件记录(eml)**，枚举 `ad/common/constants/AdAttachmentType`
- 提交守卫（**后端 `AdOrderService.submit` 与前端 `create.vue handleSave('submit')` 必须同步**）：有 type=60 → 豁免 10/20；无 → 10+20 必传
- `AdOrderAttachmentService.upload` 对 60 强制 `.eml` 后缀
- UI：create.vue 附件区 `n-tabs`（普通附件 / 邮件记录(eml)），eml 仅下载+删除；detail.vue 草稿态加 eml 上传按钮 + `attTypeLabel` 60

## 当前状态（2026-09-16）
- 最近提交：`8550444fb` 复制订单、`827e67763/926fc2fd6` 发票附件、`03ca30213` 合同作废审批
- Flyway 3.0.0 已用到 41，新增须 >41
- 工作区未提交：eml 改造（后端 4 个 ad 文件 + 前端 create/detail）+ 复制功能 sql（已 staged）
