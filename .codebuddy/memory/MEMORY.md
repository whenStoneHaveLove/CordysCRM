# CordysCRM 项目记忆

## 项目概述
- **FLOW**：飞致云开源的新一代 AI CRM 系统（GitHub: 1Panel-dev/CordysCRM）
- **协议**：FIT2CLOUD Open Source License（本质 GPLv3，不可替换 Logo 和版权信息）
- **当前分支**：dev3.0（基于开源版本二次开发）

## Git 提交（重要约定）
- **提交时必须加 `--no-verify`**！项目配置了 husky pre-commit hooks，会跑 `type:check`（vue-tsc）、`lint-staged` 等，会卡住或报 `Cannot find type definition file for 'vite/client'` 错误
- 命令：`git commit --no-verify -m "..."`
- **cmd 回显中文提交信息会显示成乱码（如"璁㈠崟..."）**：这是 GBK 代码页对 UTF-8 字节的错误显示，**提交内容本身是正确的 UTF-8**。验证方法：`git log -1 --format=%B > tmp.txt` 再用 read_file 读（按 UTF-8 解析）即可看到正常中文，不要用 cmd 回显判断
- 提交前先 `git status --porcelain` 检查暂存区；上次会话常遗留 `build_compile.bat` / `tsc_check.bat` / `compile_out.txt` / `eslint_out.txt` / `tsc_out.txt` 等临时文件在 staging 区，需用 `git restore --staged <file>` 剔除后再精准 add

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

## 自定义 BaseMapper（cn.cordys.mybatis.BaseMapper）API 速查
- 项目**不是**标准 MyBatis-Plus，自定义 `BaseMapper` 位于 `backend/framework/.../cn/cordys/mybatis/BaseMapper.java`。
- Lambda 查询入口方法名叫 **`selectListByLambda(@Param("wrapper") LambdaQueryWrapper<E> wrapper)`** —— **不是** `selectByLambda` 也**没有** `selectList(null)`（标准 MP 才有）。
- 全量查询：`selectAll(String orderBy)`（orderBy 可传 null/空，blank 时按主键 DESC）。
- 按主键更新：`updateById(E entity)`（存在）。
- `LambdaQueryWrapper` 全限定名 `cn.cordys.mybatis.lambda.LambdaQueryWrapper`，有 `.eq(实体::getXxx, value)` 等方法。
- 踩坑：2026-08-31 曾误用 `selectList(null)`/`selectByLambda` 导致编译失败，正确为 `selectListByLambda`。
- **巨坑（2026-09-01）：`BaseMapper.select(E criteria)` 会把 int 字段默认值 0 当 WHERE 条件**。`AbstractSqlProviderSupport.tableWhere` 用 `<if test="字段 != null">`，而 `status/paymentDone/receiptDone/...` 这些 int 默认 0 ≠ null，会被自动拼进 WHERE → 全表只剩 status=0 等符合默认值的"草稿"数据。**全表扫描严禁直接 `new E() + setDeleted(0)`**。三个补救：
  - XML 自定义方法（按需写 WHERE）；
  - `selectAll(orderBy)` + 后置 stream 过滤（注意软删字段名可能叫 `deleted`/`is_deleted`）；
  - LambdaQueryWrapper 但只能走 `selectListByLambda`（标准 wrapper 内显式声明比较写法）。
  - 项目里 `crm/.../ad/order/mapper/ExtAdOrderMapper.xml` 已有 `selectAllNonDeleted` 可参考模板。

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

## 广告订单 Excel 导出实现（已实现并提交）
- **方案**：同步流式下载（不走「我的导出」异步抽屉）。点「导出」按钮 → 前端 `exportAdOrder`（`CDR.post` + `responseType:'blob'` + `isReturnNativeResponse:true`）→ 后端 `AdOrderController.export` 用 `EasyExcel.write(response.getOutputStream())` 直接回写，返回 `Content-disposition: attachment;filename=...`。
- **权限**：`@CsPermission(PermissionConstants.AD_ORDER_EXPORT)`，三方权限体系已一致。
- **导出列**：前端从 `columns`（排除 action 列）取 `{key, title}` 传给后端 `AdOrderPageRequest.headList`；后端按 title 在 `HEAD_VALUE_MAPPER` 匹配取值器（枚举 label、日期格式化、金额 toPlainString、收款/付款/合同状态中文）。
- **关键坑**：
  1. 后端 mapper key（中文标题）必须**严格等于**前端 columns 的 `title`（i18n 翻译结果），否则该列返回空。当前前端翻译为「应付 / 投放起始 / 投放结束」（短名），不是「应付总额 / 投放起始日 / 投放结束日」。
  2. **不要用原生 `fetch` 导出**：本项目鉴权靠请求头 `X-AUTH-TOKEN` + `CSRF-TOKEN`（非 cookie），原生 fetch 不带这俩 → 401。必须用 `CDR` 实例。
  3. 不要用 `EasyExcelExporter.exportByCustomWriteHandler(..., null)`（传 null handler → NPE）。列宽/样式用 `registerWriteHandler` + 自定义 `AbstractHeadColumnWidthStyleStrategy` 子类 / `HorizontalCellStyleStrategy`。
  4. EasyExcel 包名是 **`cn.idev.excel`**（项目用的 fork，不是 `com.alibaba.excel`）。
  5. 导出方法 return type 用 `void`，直接写 response；避免被 `ResultResponseBodyAdvice` 包装。
- 文件：`AdOrderController.java`（`export` + `buildExportHead` + `buildExportRows` + `HEAD_VALUE_MAPPER` + `DEFAULT_HEAD_TITLES` + 样式/列宽策略）、`AdOrderService.exportList`、`AdOrderPageRequest.headList`、前端 `adOrder.ts`/`requrls/adOrder.ts`/`order/index.vue`。

## 提交策略（脏 working tree 上精准 commit）
- **用户 dev3.0 工作区常常累积大量未提交的本地改动**（用户自己改的批量文案/逻辑/数据迁移）。AI 只做局部改动就"提交"时，要避免把 working tree 里其他无关文件一起 commit。
- **做法**：先列计划要 submit 的具体文件名（一般只有 1~4 个），分别 `git add <file1> <file2> <file3>`，然后 `git diff --cached --stat` 复核 staging 区是不是只有预期文件 → 再 `git commit --no-verify`。
- 若需更精细到某一行：用 `git add -p` 交互选 patch（cmd 下不友好）；或直接 `git restore --source=HEAD -- <file>` 还原该文件工作区，再手动应用本会话改动（后者会丢用户历史改动，需用户同意）。
- 如果改动文件 working tree 已包含用户未提交的内容，commit 后可以让用户确认 staged 区是否是他期望的范围。
