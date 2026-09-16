# CordysCRM 项目记忆

## 项目概述
- 飞致云开源 AI CRM（1Panel-dev/CordysCRM，FIT2CLOUD OSS ≈ GPLv3，不可替换 Logo/版权）；分支 `dev3.0`（二次开发）
- 后端：Spring Boot 3 + Java 21 + Jetty、MyBatis 自定义 BaseMapper + PageHelper、Shiro 2(Jakarta)、Redis + Redisson + Spring Session、Flyway、SpringDoc、Quartz、FastExcel
- 前端：Vue 3.5 + TS、Naive UI(web)/Vant(mobile)、Vite、pnpm monorepo、Pinia、Axios(CordysAxios)、Tailwind 3.4 + Less、ECharts、vue-i18n
- 路径：后端 `backend/crm/src/main/java/cn/cordys/`；前端 `frontend/packages/web/src/views/advertising/`；共享 `frontend/packages/lib-shared/`

## 命令 / 约定
- 编译：`cd backend` → `& "D:\apache-maven-3.9.4\bin\mvn.cmd" -pl crm -am compile -DskipTests`（系统默认 mvn 3.3.3 太老，flatten 插件要求 ≥3.6.3）
- 单测：`& "D:\apache-maven-3.9.4\bin\mvn.cmd" -pl crm test -Dtest=XxxTest -DfailIfNoTests=false`
- 前端类型检查：`pnpm --filter @cordys/web run build`（= vue-tsc --noEmit + vite build）；只查类型用 `packages/web` 下 `pnpm exec vue-tsc --noEmit`
- 输出重定向到 `$env:TEMP`（勿写仓库目录，git 会看到）；cmd 中文回显 GBK 乱码但文件内容 UTF-8 正确
- Git：必须 `--no-verify`；只 add 本次相关文件 + `git diff --cached --stat` 复核暂存区

## 核心业务
- 订单主状态：草稿0 → 已提交10 → 审批通过20 → 待执行45 → 执行中50 → 结算中80 → 已归档90；改单审核中60、作废100（改单可从 45/50/80 发起，结束后恢复原状态）
- 合同用印：未申请0 → 审批中10 → 已用印20 → 归档审批中40 → 已归档60；驳回30、归档驳回50
- 合同类型 10框架/20单笔/30服务/40其他；方向 10上游/20下游

## BaseModel / BaseMapper 易错
- `id/createUser/updateUser` VARCHAR(32)；`createTime/updateTime` BIGINT
- `insert` 全字段 INSERT；Lambda 用 `selectListByLambda`、按主键 `updateById`
- **巨坑**：`select(E criteria)` 把 int 默认 0 当条件 → 严禁 `new E()+setDeleted(0)`；改用 XML 自定义 / `selectAll`+stream
- **巨坑2**：`update()` 与 `updateById()` **都是选择性更新**（`AbstractSqlProviderSupport.updateSQL` 对每个非主键列都包 `<if test="col != null">`），null 永不落库 →「清空某列」会静默失效保留旧值；需要清空必须自定义全字段 SQL（参考 `ExtAdOrderDownstreamMediaMapper.updateFull`）。`insert` 是全字段，新建行不受影响
- 改实体前先看 DDL：`ad_order_log` 无 deleted/create_user/update_user/update_time；`ad_order_contract`/`ad_order_downstream_media` 无 create_user/update_user/update_time
- 逻辑删除 + 唯一键不含 deleted：「逻辑删 → 再插」会 Duplicate → 查全部(含已删) → 已存在则复用并置 deleted=0
- Flyway 3.0.0 已用到 41，新增须 >41；版本号全局唯一；`validate-on-migrate=false`

## 订单金额 / 收入计算链路（关键）
- **两个计算源**：`AdAmountCalculator.computeAmounts` 算**应收侧**（rebateAmount、receivableAmount、receiptPrepayAmount、paymentPrepayAmount）；`AdOrderService.applyIncomeFromPayables` 算**应付侧+收入**（mediaPayableAmount、actualMediaPayableAmount、mediaRebateAmount、orderIncomeAmount=实际应收−实际应付、paymentMethod 由明细推导）
- 收入三列唯一口径：实际应付 = Σ明细 actual_payable（null 兜底「应付−返点」）；应付返点 = mediaPayableAmount − 实际应付；订单收入 = receivableAmount − 实际应付
- 列表/详情**直接读库列**（`order_income_amount` 等），列不对就展示陈旧值；补数走 `POST /api/ad/order/recompute-income` → `recomputeIncomeFieldsForAll`
- `AdOrderService.recomputeIncomeFromDetails(order)`：以库中明细重算收入三列（仅赋值不落库），改单执行收口调用
- **改单 execute 顺序（2026-09-16 修复）**：applyAfter → syncOrderDownstreamMedia(明细) → computeAmounts(应收) → recomputeIncomeFromDetails(收入三列)。顺序敏感：明细须先于应收（预付基数=应付），收入须后于应收（基数=应收）
- **L-04 资金侧（红冲标记/应退款/待补收）本期明确不做**，javadoc 已注明；原 3 个 L-04 单测已删除
- `accountPeriodStartDate/accountPeriodEndDate` 全项目无赋值点（注释写"自动"，实际没人算）

## 广告模块
- 合同-订单关联走 `ad_order_contract`；订单页合同下拉只列 `sealStatus=60`
- 订单编辑回填 `loadForEdit`：用 `isRestoringFromDetail` 标志避免 watch 清空 `contractId`
- 操作日志 i18n：后端英文 module/action 须同步 `config/adLog.ts`、`zh-CN.ts`、`en-US.ts`
- 附件：`processTemp` 会删除「同 resourceId 下不在 tempFileIds 里」的附件 → 逐个追加必须用 `appendTemp`；类型 10排期/20邮件截图/40过程/60邮件记录(eml)；有 type=60 时提交豁免 10+20（后端 `submit` 与前端 `create.vue` 守卫必须同步）
- eml 预览：服务端 `AdOrderAttachmentService.previewEml`（jakarta.mail 解析，内嵌图转 data URL）→ 前端 `<iframe sandbox="" :srcdoc>`；前端无附件类型枚举，沿用 60 字面量
- 预览鉴权：`/attachment/**` 走 `FileAccessAuthFilter`，只认 Cookie `F_A_TOKEN`（AES(sessionId, secret)）+ Redis session 有效
- 导出：`EasyExcel.write(response.getOutputStream())` 直写、方法 return void；中文 mapper key 必须 = 前端 columns 的 title
- 复制订单：不含付款字段（付款方式/预付/后付由下游客户明细承载，`syncOrderDownstreamMedia` 依明细推导主表 `paymentMethod`）

## 前端避坑
- **iframe 的 `sandbox` 必须写 `sandbox=""`**：裸属性被 vue-tsc 判为 boolean → TS2322 构建失败
- 构建前 `$env:NODE_OPTIONS=""`：被 IDEA 注入 `node-language-shim`（拦截 fs 删除）会卡死 vite `emptyOutDir`
- eslint 全绿 ≠ 能过 vue-tsc，提交前务必跑类型检查
- eslint 修复：`pnpm exec eslint --fix --config packages/web/eslint.config.cjs <file>`（勿用 npx）
- `n-modal preset="card"` 需手动 `<template #footer>`；未开 autoImport，组件须显式 import
- `n-upload` 无 `slots.file`，用 `:show-file-list="false"` 自渲染；`custom-request` 须按 `f.file === rawFile` 回写
- 既有 lint error（勿顺手改以免污染 diff）：`lib-shared/models/advertising.ts` 空接口 `AdBusinessEntityDetail`

## 当前状态（2026-09-16）
- 最近提交：`039709c9b` 修 eml sandbox、`597b01b6a` eml 预览、`d8eb52090` 复制弹窗移除付款分组、`8550444fb` 复制订单、`827e67763/926fc2fd6` 发票附件
- 未提交：改单执行收入侧重算（`AdOrderService.recomputeIncomeFromDetails` 新增 + `AdOrderChangeService.execute` 顺序修正 + `AdOrderChangeServiceTest` 重写），测试 8/8 绿；另有 `.codebuddy/memory/*.md`
