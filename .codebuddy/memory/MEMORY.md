# CordysCRM 项目记忆

## 项目概述
- **Cordys CRM**：飞致云开源的新一代 AI CRM 系统（GitHub: 1Panel-dev/CordysCRM）
- **协议**：FIT2CLOUD Open Source License（本质 GPLv3，不可替换 Logo 和版权信息）
- **当前分支**：dev3.0（基于开源版本二次开发）

## Git 提交（重要约定）
- **提交时必须加 `--no-verify`**！项目配置了 husky pre-commit hooks，会跑 `type:check`（vue-tsc）、`lint-staged` 等，会卡住或报 `Cannot find type definition file for 'vite/client'` 错误
- 命令：`git commit --no-verify -m "..."`

## 操作日志（ad_operation_log）i18n 配置
- 写入后端的 `module` / `action` 是英文枚举值，前端必须配中文翻译才能在列表正确显示
- 配置文件：`frontend/packages/web/src/config/adLog.ts`
- 翻译文件：`frontend/packages/web/src/views/advertising/locale/zh-CN.ts`、`en-US.ts`
- **新增后端操作时必须同步三处**：`adLog.ts` 加 value+label key、zh-CN.ts 加翻译、en-US.ts 加翻译
- 当前 action：CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE/VOID/FORCE_ARCHIVE/FINANCIAL_PRE_ACTION/CONFIRM_EXECUTE/COMPLETE_EXECUTE/DISABLE/APPLY/CLOSE/RECEIVE/PAY_MEDIA_PREPAY/PAY_MEDIA_POSTPAY/RED_INVOICE_CLEAR/CANCEL/CHANGE_REFUND/CHANGE_BAD_DEBT/SUBMIT_ARCHIVE/APPROVE_ARCHIVE/REJECT_ARCHIVE/OVERDUE_AUTO_50_TO_80
- module：AD_ORDER / AD_ORDER_CHANGE / AD_CONTRACT / AD_CUSTOMER / AD_BUSINESS_ENTITY / AD_DOWNSTREAM_MEDIA / AD_UPSTREAM_AGENT / AD_PAYMENT / AD_SEAL / AD_SUPPORT / AD_DICT

## 技术栈
| 后端 | 前端 |
|------|------|
| Spring Boot 3.x + Java 21 | Vue 3.5 + TypeScript 5.9 |
| Jetty | Naive UI 2.44+ (Web) / Vant 4.8+ (Mobile) |
| MyBatis + 自定义 BaseMapper | Vite 7 (web) / Vite 6 (mobile) |
| PageHelper | pnpm monorepo |
| Apache Shiro 2.x (Jakarta) | Vue Router 4.5 (Hash) |
| Redis + Redisson + Spring Session | Pinia 2.3 + persistedstate |
| Flyway | Axios 1.7 (CordysAxios) |
| SpringDoc / Swagger 3 | Tailwind CSS 3.4 + Less |
| Quartz | ECharts 6 + vue-echarts |
| FastExcel | vue-i18n 9 |
| Auth0 java-jwt | |

## 项目结构
- 后端：`backend/crm/src/main/java/cn/cordys/` 下 `config` / `common` / `crm` / `ad`
- 前端 Web：`frontend/packages/web/src/views/advertising/`
- 共享库：`frontend/packages/lib-shared/`

## 核心业务流程
- **订单生命周期**：草稿(0) → 已提交(10) → 审批通过(20) → 执行中(30/40) → 执行完成(50) → 已归档(60)；可审批驳回(15)、作废(70)、强制归档(80)
- **合同用印状态**：未申请(0) → 审批中(10) → 已用印(20) → 归档审批中(40) → 已归档(60)；可已驳回(30)、归档驳回(50)
- **合同类型**：10框架 20单笔 30服务 40其他
- **合同方向**：10上游（我方甲方） 20下游（我方乙方）

## 开发原则
- **改实体类之前必须先看 DDL**：不是所有 `ad_*` 表都有 BaseModel 的全部字段
- **局部问题局部修**：不要因 INSERT 报错就去掉 `extends BaseModel`
- **新增 Flyway 迁移前检查版本号是否冲突**，版本号全局唯一（不区分 ddl/dml 目录）
- **附件路径**：`/opt/cordys/data/files`，`tmp/` 临时、`pic/{orgId}/` 正式；`TaskCleanupJob` 每天 3 点触发但 `deleteOnExit` 是 JVM 退出才删

## Naive UI 避坑
- **表格列宽**：`scroll-x` 必须 ≥ 所有列宽之和（含 fixed 列）+ 50~100 余量
- **弹窗按钮**：`n-modal preset="card"` 不会自动渲染底部按钮，必须手动 `<template #footer>`
- **弹窗输入框不可见**：`naive-ui` 不开启 autoImport，必须在 `script setup` 里显式 `import { NInput } from 'naive-ui'`
- **上传组件**：`custom-request` 模式下不要手动 push file-list；回显项必须带 `file` 字段（可用 `new File([], name)` 占位）

## 附件上传/预览/下载
- 创建表单内暂存附件（pendingFiles ref），创建订单后用 orderId 批量上传
- 提交时前后端都校验盖章排期(type=10)和邮件截图(type=20)必传
- 预览：`Content-Disposition: inline`；下载：`attachment`
- Shiro 白名单：`/attachment/download/**`
- 前端预览路径：`/attachment/preview/{id}?userId=xxx`

## ESLint simple-import-sort 排序规则
- **以 `packages/web/eslint.config.cjs` 为准**
- 分组：node 依赖 → `@/assets` → `@lib/shared/.*` → 组件 → `@/` 公共模块 → `@/models`/`@/enums` → `^type`
- 修 import 排序：`pnpm exec eslint --fix --config packages/web/eslint.config.cjs <file>`（不要用 npx）

## BaseModel 字段
```java
id (Long)
createUser (Long)
updateUser (Long)
createTime (LocalDateTime)
updateTime (LocalDateTime)
```
注意：部分 `ad_*` 表无其中某些字段，需看 DDL。

## 表结构速查（ad_* 不含完整 BaseModel 的表）
- `ad_order_log`：无 deleted、create_user、update_user、update_time
- `ad_order_contract`：无 create_user、update_user、update_time（有 deleted、create_time）
- `ad_order_downstream_media`：无 create_user、update_user、update_time（有 deleted、create_time）——实体类不继承 BaseModel

## 广告模块关键实现约定
- **合同-订单关联**：统一走 `ad_order_contract` 中间表，一个合同可关联多个订单，订单端当前 UI 仅支持单选
- **订单编辑回填**：`loadForEdit` 回填 `orderType` 时会触发 watch，需加 `isRestoringFromDetail` 等标志避免清空 `contractId`
- **合同附件转存**：用印附件和双盖附件保存时调用 `AttachmentService.processTemp` 从临时目录转入正式目录
- **合同下拉**：订单页只列出 `sealStatus=60`（已归档/双盖完成）的合同
