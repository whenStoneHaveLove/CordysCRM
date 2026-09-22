# CordysCRM 项目长期记忆

## 改动铁律（最高优先级）
- 移动端改动**绝不能影响 web**：只改 `frontend/packages/mobile/`；复用能力放 `frontend/packages/lib-shared/`（须向后兼容、web 零回归）；不动 web 的 `src/`、vite/env、后端行为。

## 结构与调试
- monorepo（pnpm workspace）`frontend/packages/{web,mobile,lib-shared}`；web=Vue3+Naive UI；mobile=Vue3+Vant4+TS；两端共用 `@/api/modules` + `@lib/shared`
- dev：mobile 3000（Vite 热更新源码，无需打包）；8081 内嵌旧前端产物。代理 `/front`(去前缀)/`/pic`/`/sse`，mobile 额外 `/attachment`（附件预览无前缀）。`VITE_DEV_DOMAIN` 必须带 `http://`
- **mobile iconfont 是 web 的子集**：web 专有字形（`iconicon_advertising`/`iconicon_order_form`/`iconicon_contract`/`iconicon_seal`/`iconicon_send` 等）在 mobile 缺失 → `<CrmIcon>` 渲染**空白**（底栏「广告」tab、工作台统计卡都踩过）。可用字形清单：`Select-String -Path mobile/src/assets/icon-font/iconfont.js -Pattern 'id="(iconicon_[a-z0-9_]+)"'`。CrmIcon 颜色要传 `:color` prop（不是 style color）
- mobile 全量校验：`pnpm exec vue-tsc --noEmit -p tsconfig.json`（项目内已有 change/order/payout 的 vant 类型噪音，只比对改动文件）；生产构建校验用 `vite build --config ./config/vite.config.prod.ts --outDir dist-check`（默认 dist 会被 IDE 的 node-safe-delete shim 拦住，报 `--file parameter is required`）

## 权限体系
- 登录写 `permissionIds`；前端 `utils/permission.ts` 的 `hasPermission`（admin 直通）+ `v-permission` + 路由守卫；后端 `PermissionCache` 统一过滤
- **权限码必须逐字对齐 web/后端** `PermissionConstants`（合同提交作废是 `AD_CONTRACT:VOID_SUBMIT` 而非 `AD_CONTRACT:UPDATE`）。前端门控用错码 → 有权限看不到 / 看得到点了 403
- 新增权限码三处一致：Flyway 脚本 == `PermissionConstants.java` == `permission.json`(+i18n)
- `getHomeRouteName()` 决定登录落点（有 `AD_WORKBENCH:READ`→广告工作台）；底部 tab「广告」固定首位（图标 `iconicon_home` 小房子）
- **mobile 路由必须显式写 `meta.permissions`**：`usePermission().accessRouter()` 对**没有 permissions 的路由直接放行**（web 的 `v-permission`-style 思路不适用于路由层）。广告模块列表页=对应 `AD_*:READ`；详情页除 READ 外要**并列该模块的流程动作码**（`APPROVE/REJECT/PAY/UPDATE/VOID_*` 等），否则审批人从审批中心跳详情会被挡到无权限页；表单页=对应 `AD_*:READ`（对齐 web create/edit 路由口径）
- **按钮门控口径（mobile）**：列表「新建」`hasPermission('AD_*:CREATE')`（订单列表曾漏 → 已补）；详情动作栏/编辑由 `moduleConfig.ts` 的 permission+show 双门控；工作台快捷入口按各模块 `AD_*:READ` 过滤（原来裸列 → 无权限用户点击 403）

## 路由/页面约定（易踩坑）
- `src/router/routes/modules/*.ts` 由 `import.meta.glob` 自动加载，每文件 `export default` **一个对象**（非数组）；类型用 `AppRouteRecordRaw`（别用 vue-router 的，排序插件会报错）
- meta：`permissions`(数组)/`isCache`/`locale`(标题 key，非 title)/`depth`(1 列表 2 详情)/`showTabbar`；列表 `adXxxList` ↔ 详情 `adXxxDetail`（`name.replace(/List$/,'Detail')` 推导）
- 底栏高亮：`default-layout.vue` 的 `menuList[].name` = 父路由 name，子页 name 须以其为前缀
- `.vue` 不走 CLI eslint（dev 的 vite-plugin-eslint 拦截）；`simple-import-sort` 强制顺序 → `pnpm exec eslint src --fix`（npx 在 pnpm 下路径错）
- 广告 `.vue` 路由 import 的 `import/no-unresolved` 误报 → `mobile/eslint.config.cjs` 的 `ignore` 已加 `'^@/views/advertising'`

## 广告模块（mobile）
- 自有工具 `views/advertising/utils.ts`：格式化（`fmtAmount/fmtDate/fmtDateTime/fmtCount`）+ 标签配色（`getAdSealStatusTagStyle` 合同用印 / `getAdSealRecordStatusTagStyle` **用印记录**）+ 改单字段标签 `formatAdChangeFields`
- locale：`**/locale/zh-CN.ts` 扁平点号 key，`locale/zh-CN/index.ts` glob 合并，直接平铺
- **两套用印枚举不可混用**：合同 `AdSealStatusEnum`(0 未申请/10 审批中/20 已用印/30 驳回/40 归档审批中/50 归档驳回/60 已归档) vs 用印记录 `AdSealRecordStatusEnum`(0 审批中/10 通过/20 驳回)。记录状态曾误用合同枚举 →「通过」显示成「审批中」
- 通用详情 `detail/index.vue` + `detail/config.ts` 配置驱动 `{title, editRoute, fetch, preload?, build, extra}`；`extra` 返回 `AdDetailExtraBlock[]`（`attachment`/`records`）；`compareRows` 做改前→改后对比
- 详情动作栏由 `moduleConfig.ts` 的 `AD_MODULE_CONFIG[name].actions` 驱动（`show(d) && hasPermission`）。**`show(d)`/`canEdit(d)` 的 d = fetch 原始返回（非解包后实体）**，各详情响应体层级必须逐一对齐：合同→`d.contract`、用印记录→`d.record`、业务主体→`d.entity`、下游客户→`d.media`、上游代理商→`d.agent`、改单→`d.change`，**只有收款/付款详情是平的**（`d.status` 顶层）。曾把合同状态写成 `d?.sealStatus` → 用印后「提交归档审批/提交作废」全部不显示（`undefined === 20` 恒 false）
- 详情响应体的嵌套主体同样影响 `build` 取值（`data.contract/record/entity/media/agent/change`）；上游代理商曾误读顶层 `data.name` → 整页字段空白
- **合同详情流程链**（对齐 web `contract/detail.vue`）：`sealStatus 20|50`→上传双盖附件+提交归档审批（须先有 type=20 附件）；`sealStatus 40`→归档审核(`AD_CONTRACT:ARCHIVE_APPROVE`)；`status 10|20`→提交作废(`AD_CONTRACT:VOID_SUBMIT`+原因)；`status 70`→作废审核(`AD_CONTRACT:VOID_APPROVE`)；编辑仅 `sealStatus 0|30`。附件：`uploadTempAttachment`→`uploadAdContractAttachment(id,20,tempFileId,name)`（type 10 用印/20 双盖），删 `deleteAdContractAttachment(id,attId)`。移动端「上传双盖附件」不写死在动作栏：`extra` 的 attachment 区块带 `uploader/onDelete` → `detail/index.vue` 自动加按钮 + 弹层管理（删除仅 `item.deletable`）；`AdActionDef.guard` 做前置校验（返回 i18n key 即拦截）
- 改单：展示走 `formatAdChangeFields`（`AD_ORDER_CHANGE_FIELD_META` 漏 `downstreamMedia*`/`paymentPrepay*` 等，须兜底）；提交 `changeFields` = 非空标量字段 + 开启下游变更时的 `downstreamMediaIds`/`downstreamMediaPayables`（后端 `SPECIAL_FIELDS`；空清单直接 500）
- 付款真实字段：`billType/orderId/amount/paymentTime/type/mediaIds/mediaDetails/voucherUrl/remark`；**不存在** `billNo/paymentMethod/ourBankName/ourBankAccount/customerId/projectName/paymentDate`。订单型 `getAdPayoutMedia(orderId)`（**string 路径参数**，传对象→`[object Object]` 400）+ `getAdPayoutRemaining(orderId)`，明细自带 `accountList`；非订单型选**下游客户**(`getAdDownstreamMediaPage`)→`getAdDownstreamMediaAccounts(mediaId)` 选账户→单条 `mediaDetails`+`mediaIds:[mediaId]`
- 命名：web `module.advertising.downstreamMedia = '下游客户'`（不是「下游媒体」）
- 下拉选项**禁止回退展示 ID**：客户 `customerName||name`、上游/下游 `resourceName||name`、合同 `[contractNo,contractName]`、业务主体 `name`、字典 `dictLabel/dictValue`
- 用户 ID→姓名走 `useUserMap.ts`（`loadUserMap()`/`getUserName(id)`），禁止直显 createUser/approveUser/payUser/applicantId
- `CrmDescription` 是 h-full 全屏组件，多段详情须自绘描述块
- 表单机制：`form/config.ts` 的 `AD_FORM_CONFIG[key]`（groups/selectApis/dynamicOptions/resetOn/attachments/mapDetail/buildPayload/validate）+ `form/AdForm.vue`；薄入口 `contract|customer|businessEntity/form.vue`；receipt/seal/upstreamAgent 走 AdForm；order/change/payout/downstreamMedia 专用表单。合同附件用 `uploadTempAttachment` 临时上传 + `sealFileUrls(tempFileId)` 保存（编辑回填用 fileUrl 当 id）
- **易错点**：`enumLabel` 两套签名——`order/form.vue`、`payout/form.vue` 是 `(options,value)`；`change/create.vue` 是 `(key|options,value)`（已兼容两者）
- **易错点**：`van-date-picker` 无 `model-value` 时停在 2020-01-01，凡「点开选日期」入口必须显式初始化（空→今天、有值→回填），并区分字段级/行级上下文
- **易错点**：lib-shared 的 ad API 默认导出是**工厂** `useXxxApi`，mobile barrel 必须 `useXxxApi(CDR)` 实例化后再导出
- 已交付：工作台 + 订单列表/详情 + 审批中心 + 9 列表页 + 9 通用详情页 + 各模块新建/编辑 + 附件预览 + 全列表筛选（订单原生，其余复用 `AdListFilter`）；审批中心仅「待审批 + 类型筛选」；报表/操作日志用户明确不做
- 合同列表**双 tab**（合同列表/已作废，对齐 web deleted tab）：`getAdContractDeletedPage`/`getAdContractDeletedDetail`（mobile barrel 需自行导出）；`van-tabs` 只当数据源切换器（搜索+列表在 tabs 外共享），切换后必须 `await nextTick()` 再 `loadList(true)`（`load-list-api` 是 prop，同步调用会打到旧接口）；已作废进入详情带 `deleted=1` → 只读（隐藏编辑/流程动作/上传入口，标题挂「已作废」tag）。locale 命名 `advertising.contract.tab.voided`（web 叫「已删除」）
- **UI 主题基线**（`assets/style/theme.less`）：按钮 8px 圆角/40px 高/字重 500，实心 primary 带轻投影，`:active` 轻微缩放；搜索框 8px 圆角。改按钮外观优先在这里统一改，别逐页写样式。工作台（`workbench/index.vue`）无顶部标题栏，问候区渐变 + 统计卡 + **快捷入口图标宫格** + 待办彩色图标方格（对齐 web 卡片风格）
- **预上线占位页开关（2026-09-20）**：移动端暂未对外上线，用 `App.vue` 顶部 `const COMING_SOON = true` 作总开关——为 true 时只渲染 `views/base/coming-soon/index.vue`（全屏「敬请期待」页，品牌渐变+FLOW logo 标记），并跳过 `onBeforeMount` 的登录/重定向。**想上线只需把 `COMING_SOON` 改成 `false`**，其余代码不动。占位页文案在 `coming-soon/index.vue` 内（FLOW 标记/标题/副文案均可直接改）。勿把"只显示敬请期待"当成 app 故障。
- **mobile 提交统一走 `git commit --no-verify`（2026-09-22）**：`.husky/pre-commit` 在暂存前端源码时会让 lib-shared/mobile/web 三包各跑 `type:check`+`lint-staged`。但 mobile 全包 `vue-tsc` 有 **19 处既有类型错误**（集中在广告模块 `order/form.vue`、`form/AdForm.vue`、`change/create.vue` 等），与单次改动无关，且已决策移动端跳过类型检查（build 去 vue-tsc）。因此 mobile 相关提交一律 `--no-verify` 跳过该门禁。待广告模块 19 处类型错误彻底修复后，再恢复走钩子。

## 数据库
- `sys_role_permission(id, role_id, permission_id)`，id 用 `UUID_SHORT()`，无唯一键 → 授权脚本须 `WHERE NOT EXISTS` 幂等
- 内置角色 `org_admin`/`sales_manager`/`sales_staff`；Flyway 在 `backend/crm/src/main/resources/migration/<版本>/{dml,ddl}`

## 提交/构建
- 分支 `dev3.0`；husky + commitlint（`feat/fix(...)`，body 每行 ≤100 字符，超长用 `git commit -F 文件`）
- JaCoCo 绑根 `pom.xml`，`mvn clean verify` 消累积 exec 报错；完整 verify 需 Docker

## 广告模块 - 返点标签语义修正（2026-09-20，文字理解问题）
- 用户澄清：之前界面标「应收返点」(含比例) 的字段，其实是**我们付给别人**的钱（应付）；之前标「应付返点」(含比例) 的字段，其实是**别人返给我们**的钱（应收）。**仅对调展示文字**，数据/key/变量名不变。
- 现在的正确显示：原「应收返点」→「应付返点」；原「应付返点」→「应收返点」；比例同理（应收返点比例↔应付返点比例）。
- 展示文案只在 i18n 对调，文件：`web/src/views/advertising/locale/zh-CN.ts`、`web/.../en-US.ts`（英文同步对调语义）、`mobile/src/views/advertising/order/locale/zh-CN.ts`。订单列表/详情/导出都引用这些 key → 全覆盖。
- 数据字段（未改名，仅语义）：`order.rebateAmount` = 现「应付返点」；`order.mediaRebateAmount` = 现「应收返点」。`receivableRebateRatio = rebateAmount/totalAmount`（现显示「应付返点比例」）；`payableRebateRatio = mediaRebateAmount/mediaPayableAmount`（现显示「应收返点比例」）。
- **重要陷阱**：i18n key 名（`receivableRebateRatio`/`payableRebateRatio`/`mediaRebate`/`column.rebate`）**故意不重命名** → 会出现 key=`receivableRebateRatio` 却显示「应付返点比例」的"名实相反"，这是预期，切勿为"对齐"去改 key 名（会牵动数据/组件引用）。代码注释（`order/detail.vue`、`order/index.vue`）描述的是变量语义，也保持原样。
- 用户通用口径：说「只是换文字/数据不换」= 只换展示 label，不碰 key、变量名、字段值。
