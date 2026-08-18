# CordysCRM 项目记忆

## 项目概述
- **Cordys CRM**：飞致云开源的新一代 AI CRM 系统（GitHub: 1Panel-dev/CordysCRM）
- **协议**：FIT2CLOUD Open Source License（本质 GPLv3，不可替换 Logo 和版权信息）
- **当前分支**：dev3.0（基于开源版本二次开发）

## Git 提交（重要约定）
- **提交时必须加 `--no-verify`**！项目配置了 husky pre-commit hooks，会跑 `type:check`（vue-tsc）、`lint-staged` 等，会卡住或报 `Cannot find type definition file for 'vite/client'` 错误
- 命令：`git commit --no-verify -m "..."`（不要用普通 `git commit`）
- 用户明确要求：提交不要跑 hooks

## 操作日志（ad_operation_log）i18n 配置
- 写入后端的 `module` / `action` 是英文枚举值，前端必须**配中文翻译**才能在列表正确显示
- 配置文件：`frontend/packages/web/src/config/adLog.ts`（`adLogModuleOption` + `adLogActionOption`）
- 翻译文件：
  - `frontend/packages/web/src/views/advertising/locale/zh-CN.ts`（`advertising.log.action.XXX`）
  - `frontend/packages/web/src/views/advertising/locale/en-US.ts`
- 渲染逻辑：`views/advertising/log/index.vue` 里 `actionLabel(row.action)` 通过 `actionLabelMap` 找标签，找不到则回退到原始 action（**就会显示英文**）
- **新增后端操作时必须同步三处**：`adLog.ts` 加 value+label key、zh-CN.ts 加翻译、en-US.ts 加翻译，否则日志里就显示英文
- 当前所有 action：CREATE/UPDATE/DELETE/SUBMIT/APPROVE/REJECT/EXECUTE/VOID/FORCE_ARCHIVE/FINANCIAL_PRE_ACTION/CONFIRM_EXECUTE/COMPLETE_EXECUTE/DISABLE/APPLY/CLOSE/RECEIVE/PAY_MEDIA_PREPAY/PAY_MEDIA_POSTPAY/RED_INVOICE_CLEAR/CANCEL/CHANGE_REFUND/CHANGE_BAD_DEBT/SUBMIT_ARCHIVE/APPROVE_ARCHIVE/REJECT_ARCHIVE
- 所有 module：AD_ORDER / AD_ORDER_CHANGE / AD_CONTRACT / AD_CUSTOMER / AD_BUSINESS_ENTITY / AD_DOWNSTREAM_MEDIA / AD_UPSTREAM_AGENT / AD_PAYMENT / AD_SEAL / AD_SUPPORT / AD_DICT

---

## 技术栈

### 后端
| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 3.x（Java 21） |
| Web 容器 | Jetty（非 Tomcat） |
| ORM | MyBatis（非 MyBatis-Plus）+ 自定义 BaseMapper |
| 分页 | PageHelper |
| 安全 | Apache Shiro 2.x（Jakarta 版） |
| 缓存/Session | Redis + Redisson + Spring Session |
| 数据库迁移 | Flyway |
| API 文档 | SpringDoc OpenAPI (Swagger 3) |
| 定时任务 | Quartz |
| Excel | FastExcel |
| JWT | Auth0 java-jwt |

### 前端
| 类别 | 技术 |
|------|------|
| 框架 | Vue 3.5 + TypeScript 5.9 |
| UI (Web) | Naive UI 2.44+ |
| UI (Mobile) | Vant 4.8+ |
| 构建 | Vite 7 (web) / Vite 6 (mobile) |
| 包管理 | pnpm monorepo |
| 路由 | Vue Router 4.5 (Hash 模式) |
| 状态管理 | Pinia 2.3 + persistedstate |
| HTTP | Axios 1.7（自封装 CordysAxios） |
| CSS | Tailwind CSS 3.4 + Less |
| 图表 | ECharts 6 + vue-echarts |
| 国际化 | vue-i18n 9 |

---

## 项目结构

### 后端模块（Maven 多模块）
```
backend/
├── framework/          # 框架层：BaseMapper、Shiro 基础、工具类、AOP
├── crm/                # 核心业务层（所有业务代码在这里）
│   └── src/main/java/cn/cordys/
│       ├── config/     # ShiroConfig, MybatisConfig, RedisConfig 等
│       ├── common/     # 通用层：BaseModel、权限注解、上下文、拦截器
│       ├── crm/        # 原 CRM 业务模块（客户、商机、线索等）
│       └── ad/         # ★ 广告模块（二次开发核心）
└── app/                # 启动模块：Application.java
```

### 前端 monorepo
```
frontend/
├── packages/web/       # PC Web 端（主应用）
│   └── src/
│       ├── api/modules/        # 统一 API 导出
│       ├── views/advertising/  # ★ 广告模块页面
│       ├── router/routes/modules/advertising.ts  # 广告路由
│       ├── hooks/              # 组合式函数
│       ├── components/         # 业务/通用组件
│       └── layout/             # 布局
├── packages/mobile/    # 移动端
└── packages/lib-shared/ # 共享库
    ├── api/modules/    # API 定义（如 adContract.ts）
    ├── enums/          # 枚举（如 advertisingEnum.ts）
    ├── models/         # TypeScript 类型
    └── hooks/          # 共享 hooks
```

---

## 广告模块（ad）- 二次开发核心域

广告模块位于 `backend/crm/.../ad/` 和 `frontend/.../views/advertising/`，包含 14 个子模块：

| 子模块 | 说明 | 核心功能 |
|--------|------|----------|
| **order** | 广告订单管理 | 最复杂模块：草稿→提交→审批→执行→完成→归档，含改单、附件、收付款联动 |
| **contract** | 广告合同管理 | 合同 CRUD + 用印流程（7种状态）+ 双盖附件 + 归档审批 |
| **seal** | 用印管理 | 用印申请→审批→归档，独立列表页 |
| **change** | 改单管理 | 订单字段变更申请→审批→执行 |
| **payment** | 收付款管理 | 收付款记录 + 媒体财务操作（7种动作） |
| customer | 客户管理 | CRUD |
| businessEntity | 业务主体 | CRUD |
| downstreamMedia | 下游媒体 | CRUD |
| upstreamAgent | 上游代理 | CRUD |
| approval | 审批中心 | 订单/改单/用印 三 Tab 审批 |
| report | 报表统计 | 6类报表卡片 |
| workbench | 工作台 | 待办事项 |
| log | 操作日志 | 操作日志+登录日志 |
| dict | 数据字典 | 字典管理 |

---

## 核心业务流程

### 订单生命周期
```
草稿(0) → 已提交(10) → 审批通过(20) → 执行中(30) → 执行中(40) → 执行完成(50) → 已归档(60)
                    ↓                      ↓
                 审批驳回(15)           可作废→已作废(70)
                                         可强制归档(80)
```

### 合同用印状态
```
未申请(0) → 审批中(10) → 已用印(20) → 归档审批中(40) → 已归档(60)
              ↓                          ↓
           已驳回(30)                 归档驳回(50)
```

### 合同类型
- 10=框架合同，20=单笔合同（关联订单必填），30=服务合同，40=其他

### 合同方向
- 10=上游（我方为甲方），20=下游（我方为乙方）

---

## 开发原则（重要）
- **改实体类之前必须先看 DDL**：确认表里实际有哪些列，不能假设 BaseModel 的字段都在表里
- **局部问题局部修**：不要因为 INSERT 报错就去改实体类的继承关系（如去掉 extends BaseModel），这会影响所有用到该实体的地方。正确的做法是检查调用方代码，去掉表里不存在的字段赋值
- **先分析再动手**：遇到报错先看堆栈找到真正的根因，再决定修改范围

---

## Naive UI 表格（n-data-table）列宽与固定列
- **加列后横向滚动条拉不到最右列**：`scroll-x` 的值必须 **≥ 所有列宽之和（含 fixed 列）**，否则最右的普通列会被 `fixed: right` 列遮挡、永远显示不全
- 计算方法：`scroll-x = 非固定列宽总和 + 固定列宽总和 + 余量`（约 +50~100px）
- 例：订单列表 15 列，非固定列 1870 + 固定列(fixed:right) 420 = 2290，`scroll-x` 设 2350
- **不要**用「给列加 fixed:right」或「移动列位置」来解决被裁问题——正确做法就是调大 `scroll-x`
- 用户偏好：订单列表的「创建时间」列保持普通列（不固定），固定列只有 收款状态/付款状态/缺合同/详情

## Naive UI 弹窗按钮
- `n-modal` 使用 `preset="card"` 时**不会**自动渲染底部按钮，`positive-text`/`negative-text`/`@positive-click`/`@negative-click` 均无效
- 必须使用 `<template #footer>` 插槽手动添加按钮

## Naive UI 弹窗里的输入框渲染坑（彻底解决）
- **场景**：合同归档审核弹窗。代码看着和订单"审核通过"一致，但 textarea 不可见
- **真正原因**：`<n-input>` 没有显式 import！项目里 `naive-ui` 不开启 `autoImport`，所有用到的组件必须在 `script setup` 里 `import { NInput } from 'naive-ui'`
- **症状**：标签能渲染（普通 div 文字），但 `<n-input>` 部分整块塌陷成 0 高度
- **可靠方案**：
  ```vue
  <n-modal preset="card" :title="..." style="width: 480px">
    <n-space vertical>
      <div>
        <div class="action-modal-label">标签</div>
        <n-input v-model:value="..." type="textarea" :rows="3" placeholder="..." />
      </div>
    </n-space>
    <template #footer>...</template>
  </n-modal>

  <style scoped>
    .action-modal-label {
      margin-bottom: 4px;
      font-size: 12px;
      color: var(--text-n2);
    }
  </style>
  ```
- **排查流程**：
  1. 先确认 `<n-input>` 已在 `import { ... } from 'naive-ui'` 中导入
  2. 必须用 `<n-space vertical>` 包裹内容
  3. 标签使用 `class="action-modal-label"`（12px 小字、灰）

## Naive UI 上传（n-upload）避坑
- `n-upload` + `v-model:file-list` + `custom-request` 模式下：框架**已自动**把选中文件加入 file-list，不要手动再 push（否则"点一个出两个"）；删除时通过 `opts.file.id` 匹配
- **file-list 每一项必须带 `file` 字段**。若缺省，naive-ui 内部 `createSettledFileInfo` 会把它规范化为 `null`，渲染文件项时 `isImageFile(null)` 读 `null.type` 抛 `TypeError`，导致整个组件渲染崩溃、按钮全部失灵
- 已上传附件（无真实 File 对象）回显时，给 `file: new File([], name)` 占位即可避免崩溃
- 排查"编辑页进不去/按钮失灵"时，优先看 Console 是否有渲染期抛错（如 null.type）

---

## MyBatis Mapper XML 放置规则
- Mapper XML 文件必须放在 Java Mapper 接口的同一目录下

## 表结构速查（ad_* 表不含 BaseModel 部分字段的情况）
- `ad_order_log`：无 deleted、create_user、update_user、update_time
- `ad_order_contract`：无 create_user、update_user、update_time（有 deleted、create_time）
- `ad_order_downstream_media`：无 create_user、update_user、update_time（有 deleted、create_time）——实体类不继承 BaseModel

---

## 附件上传/预览/下载
- 创建表单内暂存附件（pendingFiles ref），创建订单后用 orderId 批量上传
- 提交时前端校验盖章排期(type=10)和邮件截图(type=20)必传
- 后端提交接口 `AdOrderService.submit()` 也有 `requireAttachment` 守卫
- 预览用 `Content-Disposition: inline`，下载用 `attachment`
- 图片类型要设正确的 Content-Type（image/png 等）
- Shiro 需要把 `/attachment/download/**` 加入白名单（authf filter）
- 前端用相对路径 `/attachment/preview/{id}?userId=xxx`

---

## ESLint simple-import-sort 排序规则
- **以 `packages/web/eslint.config.cjs` 为准**（flat config，比 lib-shared 的 `.eslintrc.cjs` 多了 `^@lib/shared/.*` 组和 `^echarts/*` 组）
- 分组顺序：
  1. node 依赖：vue/vue-router/naive-ui/echarts（含 `echarts/charts`、`echarts/core` 等子路径，按字母序）
  2. `@/assets`
  3. `@lib/shared/.*`（独立组）
  4. 组件：`@/components/pure/.*`（如 CrmIcon）、`.*\.vue$`
  5. `@/` 公共模块：@/api、@/store、@/utils（按字母序）
  6. `@/models`、`@/enums`
  7. `^type`
- 同组内 `import type` 放最后
- 修 import 排序最快方式：`pnpm exec eslint --fix --config packages/web/eslint.config.cjs <file>`
- 注意：直接 `npx eslint` 会报「找不到 eslint.config」或 pnpm 路径错乱，必须用 `pnpm exec eslint --config ...`

---

## BaseModel 字段
```java
- id (Long)
- createUser (Long)
- updateUser (Long)
- createTime (LocalDateTime)
- updateTime (LocalDateTime)
```
注意：不是所有 ad_* 表都有这 5 个字段，务必先看 DDL。
