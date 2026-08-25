# CordysCRM 广告模块「五类新建表单」字段呈现分析（基于 v3.1 DDL + Java 枚举）

> 分析人：高见远（前端架构师）｜纯分析文档，不修改任何代码
> 权威数据源：`V3.0.0_1__ad_system_v31.sql` + `cn.cordys.crm.ad.**.constants/*` 枚举类 + `AdCustomer.java` 等领域类

## 0. 分析方法与约定

**判定规则（来自需求方）**
- 必填：`NOT NULL` 且无 `DEFAULT` 的列 → 用户必填；`NOT NULL` 但有默认值/由系统算 → 标记为「系统」。
- 外键列：列名以 `_id` 结尾且关联其他表（存对方表主键）→ 应当「下拉选数据、回存 ID」。
- 枚举列：`TINYINT` 且取值固定 → 下拉，选项来自对应 Java 枚举（无需 fetch，前端写死即可）。
- 字典列：COMMENT 标注「(字典)」→ 下拉，选项来自 `ad_dict` 表（需 fetch）。
- 自由文本/备注列：如 `name`、`brand`、`remark` → 自由输入。

**表格字段含义**
| 列 | 取值说明 |
|----|----|
| 存储方式 | `ID外键` / `枚举值` / `字典code` / `自由文本` / `数值` / `日期` / `文件URL` / `系统填充` |
| 应选下拉? | `是(需fetch数据)`=外键/字典，必须从后端取数；`是(静态枚举,无需fetch)`=枚举，选项固化；`否(自由输入)`；`系统自动`=系统/默认值填充 |
| 下拉数据来源 | 具体表（ad_business_entity / ad_customer / ad_resource / ad_order）、枚举类、或 `ad_dict` 的 `dict_code` |
| 必填? | `是`=NOT NULL 无默认、用户必填；`系统`=NOT NULL 但由系统/默认值填充；`否`=可空 |

> ⚠️ 前缀约定：`id`、各类 `*_time`/`*_user`、`deleted`、`organization_id`、`tenant_id`、`order_no`、订单的状态/进度/累计金额、合同 `seal_status`/`status`、用印 `applicant_id`/`status` 等一律**系统填充**，不在创建表单出现或只读，下文表中一并列出但标记为「系统自动」。

---

## 1. 枚举取值对照表（下拉选项 code→label）

| 枚举类 | 对应字段 | 取值（code→label） |
|--------|----------|--------------------|
| `OrderType` | ad_order.order_type | 10=框架合同 / 20=单笔合同 |
| `OrderStatus` | ad_order.status | 0=草稿 / 10=待管理组审核 / 20=审核通过 / 30=待确认预收款 / 40=待付预付款 / 50=执行中 / 60=变更审核中 / 70=执行完成 / 80=结算中 / 90=已归档 / 100=已作废 |
| `ReceiptMethod` | ad_order.receipt_method | 10=预付款 / 20=账期 |
| `PaymentMethod` | ad_order.payment_method | 10=预付 / 20=后付 |
| `PaymentPostpayTrigger` | ad_order.payment_postpay_trigger | 10=收到上游全款后 / 20=执行完成X天后 |
| `InvoiceStatus` | ad_order.invoice_status | 0=未开 / 10=部分 / 20=全额 |
| （同 0/10/20 语义） | ad_order.receipt_status | 0=未收 / 10=部分 / 20=全额 |
| `MediaPaymentStatus` | ad_order.media_payment_status | 0=待付 / 10=部分付 / 20=已付 |
| `ContractDirection` | ad_contract.contract_direction | 10=上游 / 20=下游 |
| `ContractType` | ad_contract.contract_type | 10=框架 / 20=单笔 |
| `SealStatus` | ad_contract.seal_status | 0=未申请 / 10=审批中 / 20=已用印 / 30=已驳回 |
| `BusinessEntityStatus` | ad_business_entity.status | 10=启用 / 20=停用 |
| `ResourceType` | ad_resource.resource_type | 10=上游代理 / 20=下游客户 |
| `CustomerStatus`（M6） | ad_customer.status（DDL 暂缺） | 0=活跃 / 10=非活跃 / 20=黑名单 |
| `CustomerLevel`（M6） | ad_customer.customer_level（DDL 暂缺） | 10=VIP / 20=普通 / 30=潜力 |
| （DDL 注释，无独立枚举类） | ad_order.rebate_mode / receipt_prepay_mode / payment_prepay_mode | 10=比例 / 20=固定金额 |
| （DDL 注释，无独立枚举类） | ad_contract.related_party_type | 10=客户 / 20=上游代理 / 30=下游客户 |
| （DDL 注释，无独立枚举类） | ad_seal_record.seal_type | 10=公章 / 20=合同章 |
| （DDL 注释，无独立枚举类） | ad_contract.status | 10=生效 / 20=失效 / 30=已作废 |
| （DDL 注释，无独立枚举类） | ad_resource.status | 10=正常 / 20=停用 |

> 字典来源说明：`ad_dict` 实际用于本五表的字典列只有两类——`industry_code`（dict_code=`industry`）与 `media_type`（dict_code=`media_type`）。DDL 在 `ad_dict.dict_code` 注释里虽列了 `seal_type/receipt_method/payment_method`，但 `seal_type`/`receipt_method`/`payment_method` 实为 Java 枚举列，**不是字典**。

---

## 2. 场景一：新建订单（ad_order）

| 字段(列名) | 中文含义 | 存储方式 | 应选下拉? | 下拉数据来源 | 必填? | 备注 |
|------------|----------|----------|-----------|--------------|-------|------|
| id | 主键 | 系统填充 | 系统自动 | — | 系统 | UUID，后端生成 |
| order_no | 订单编号 | 系统填充 | 系统自动 | — | 系统 | 规则：主体代码-YYYYMMDD-流水 |
| order_name | 订单名称 | 自由文本 | 否(自由输入) | — | **是** | NOT NULL |
| business_entity_id | 业务主体 | ID外键 | **是(需fetch数据)** | ad_business_entity | **是** | 下单必选，回存主体ID |
| customer_id | 客户 | ID外键 | **是(需fetch数据)** | ad_customer | **是** | 回存客户ID |
| industry_code | 行业类别 | 字典code | **是(需fetch数据)** | ad_dict(dict_code=industry) | 否 | 可空 |
| signing_entity | 签约主体 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| order_type | 订单类型 | 枚举值 | **是(静态枚举)** | OrderType | **是** | 10框架/20单笔 |
| upstream_agent_id | 上游代理 | ID外键 | **是(需fetch数据)** | ad_resource(资源类型=上游代理) | 否 | 选填 |
| agent_order_no | 代理订单号 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| creator_id | 下单人 | ID外键(sys_user) | 系统自动 | — | 系统 | 存 sys_user.id，=当前登录用户 |
| status | 主状态 | 枚举值 | 系统自动 | OrderStatus | 系统 | 新建默认 0=草稿 |
| total_amount | 订单总金额 | 数值 | 否(自由输入) | — | **是** | NOT NULL |
| rebate_mode | 返点方式 | 枚举值 | **是(静态枚举)** | DDL注释(10比例/20固定金额) | **是** | NOT NULL |
| rebate_value | 返点值 | 数值 | 否(自由输入) | — | **是** | NOT NULL |
| no_rebate_amount | 不记返金额 | 数值 | 否(自由输入) | — | 系统 | 默认0 |
| rebate_amount | 返点金额 | 数值 | 系统自动 | — | 系统 | 后端计算 |
| receivable_amount | 应收金额 | 数值 | 系统自动 | — | 系统 | =总额-返点，计算 |
| media_payable_amount | 应付总额 | 数值 | 否(自由输入) | — | **是** | NOT NULL（下游口径） |
| delivery_start_date | 投放起始日 | 日期 | 否(自由输入) | — | **是** | NOT NULL |
| delivery_end_date | 投放结束日 | 日期 | 否(自由输入) | — | **是** | NOT NULL |
| delivery_volume | 投放量+单位 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| remark | 备注 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| receipt_method | 收款方式 | 枚举值 | **是(静态枚举)** | ReceiptMethod | **是** | 10预付/20账期 |
| receipt_prepay_mode | 预收模式 | 枚举值 | **是(静态枚举)** | DDL注释(10比例/20固定) | 否 | 收款=预付时必填 |
| receipt_prepay_ratio | 预收比例% | 数值 | 否(自由输入) | — | 否 | 条件必填 |
| receipt_prepay_amount | 预收金额 | 数值 | 否(自由输入) | — | 否 | 条件必填 |
| receipt_prepay_deadline | 预收截止日 | 日期 | 否(自由输入) | — | 否 | 可空 |
| receipt_account_period_days | 账期天数 | 数值 | 否(自由输入) | — | 否 | 收款=账期时必填 |
| account_period_start_date | 账期起算日 | 日期 | 否(自由输入) | — | 否 | 可空 |
| account_period_end_date | 账期到期日 | 日期 | 系统自动 | — | 系统 | 计算 |
| payment_method | 付款方式 | 枚举值 | **是(静态枚举)** | PaymentMethod | **是** | 10预付/20后付 |
| payment_prepay_mode | 预付模式 | 枚举值 | **是(静态枚举)** | DDL注释(10比例/20固定) | 否 | 付款=预付时必填 |
| payment_prepay_ratio | 预付比例% | 数值 | 否(自由输入) | — | 否 | 条件必填 |
| payment_prepay_amount | 预付金额 | 数值 | 否(自由输入) | — | 否 | 条件必填 |
| payment_prepay_deadline | 预付截止日 | 日期 | 否(自由输入) | — | 否 | 可空 |
| payment_postpay_trigger | 后付触发 | 枚举值 | **是(静态枚举)** | PaymentPostpayTrigger | 否 | 付款=后付时必填 |
| payment_postpay_days | 后付X天 | 数值 | 否(自由输入) | — | 否 | trigger=20时必填 |
| invoice_status | 开票进度 | 枚举值 | 系统自动 | InvoiceStatus | 系统 | 默认0未开 |
| receipt_status | 收款进度 | 枚举值 | 系统自动 | 0/10/20语义 | 系统 | 默认0未收 |
| media_payment_status | 付款进度 | 枚举值 | 系统自动 | MediaPaymentStatus | 系统 | 默认0待付 |
| invoiced_amount | 已开票累计 | 数值 | 系统自动 | — | 系统 | 默认0 |
| received_amount | 已收款累计 | 数值 | 系统自动 | — | 系统 | 默认0 |
| media_paid_amount | 已付款累计 | 数值 | 系统自动 | — | 系统 | 默认0 |
| bad_debt_amount | 坏账金额 | 数值 | 否(自由输入) | — | 否 | 可空（后期） |
| needs_red_invoice | 需红冲标记 | 枚举值(0/1) | 否(开关,默认0) | — | 系统 | 布尔开关，非下拉 |
| currency | 币种 | 自由文本 | 否(自由输入) | — | 系统 | 默认 CNY（建议字典化） |
| tenant_id | 预留多租户 | 系统填充 | 系统自动 | — | 否 | 可空 |
| organization_id | 组织(租户)id | 系统填充 | 系统自动 | — | 系统 | 登录租户上下文 |
| create_time / update_time | 时间 | 系统填充 | 系统自动 | — | 系统 | 后端 |
| create_user / update_user | 操作人 | 系统填充 | 系统自动 | — | 系统 | 当前用户 |
| deleted | 删除标记 | 系统填充 | 系统自动 | — | 系统 | 默认0 |

**订单「需 fetch 的下拉（回存 ID）」**：business_entity_id、customer_id、upstream_agent_id（3 个外键）+ industry_code（1 个字典）。
**订单「静态枚举下拉」**：order_type、rebate_mode、receipt_method、receipt_prepay_mode、payment_method、payment_prepay_mode、payment_postpay_trigger（7 个）。

---

## 3. 场景二：新建合同（ad_contract + ad_seal_record 用印）

### 3.1 合同主表 ad_contract

| 字段(列名) | 中文含义 | 存储方式 | 应选下拉? | 下拉数据来源 | 必填? | 备注 |
|------------|----------|----------|-----------|--------------|-------|------|
| id | 主键 | 系统填充 | 系统自动 | — | 系统 | UUID |
| contract_no | 合同编号 | 自由文本 | 否(自由输入) | — | **是** | NOT NULL（疑似系统生成，需确认）|
| contract_name | 合同名称 | 自由文本 | 否(自由输入) | — | **是** | NOT NULL |
| business_entity_id | 业务主体 | ID外键 | **是(需fetch数据)** | ad_business_entity | **是** | 回存主体ID |
| contract_direction | 合同方向 | 枚举值 | **是(静态枚举)** | ContractDirection | **是** | 10上游/20下游 |
| contract_type | 合同类型 | 枚举值 | **是(静态枚举)** | ContractType | **是** | 10框架/20单笔 |
| related_party_type | 关联方类型 | 枚举值 | **是(静态枚举)** | DDL注释(10客户/20上游代理/30下游客户) | **是** | 决定 related_party_id 取数表 |
| related_party_id | 关联方 | ID外键 | **是(需fetch数据)** | ad_customer 或 ad_resource(取决于上一项) | **是** | 回存客户/资源ID |
| order_id | 关联订单 | ID外键 | **是(需fetch数据)** | ad_order | 否 | 单笔合同时必填 |
| signing_entity | 签约主体 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| valid_from | 有效期起 | 日期 | 否(自由输入) | — | 否 | 可空（建议必填）|
| valid_to | 有效期止 | 日期 | 否(自由输入) | — | 否 | 可空 |
| amount | 合同金额 | 数值 | 否(自由输入) | — | 否 | 可空 |
| rebate_terms | 返点条款 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| file_url | 合同文件 | 文件URL | 否(上传) | — | 否 | 单笔用印前可空 |
| seal_status | 用印状态 | 枚举值 | 系统自动 | SealStatus | 系统 | 默认0未申请 |
| status | 合同状态 | 枚举值 | 系统自动 | DDL注释(10生效/20失效/30已作废) | 系统 | 默认10生效 |
| organization_id | 组织id | 系统填充 | 系统自动 | — | 系统 | 租户上下文 |
| create_time / update_time | 时间 | 系统填充 | 系统自动 | — | 系统 | 后端 |
| create_user / update_user | 操作人 | 系统填充 | 系统自动 | — | 系统 | 当前用户 |
| deleted | 删除标记 | 系统填充 | 系统自动 | — | 系统 | 默认0 |

### 3.2 用印子表 ad_seal_record（合同创建后可一并发起用印申请）

| 字段(列名) | 中文含义 | 存储方式 | 应选下拉? | 下拉数据来源 | 必填? | 备注 |
|------------|----------|----------|-----------|--------------|-------|------|
| id | 主键 | 系统填充 | 系统自动 | — | 系统 | UUID |
| contract_id | 合同id | ID外键 | 系统自动 | — | 系统 | 关联新建合同 |
| seal_type | 用印类型 | 枚举值 | **是(静态枚举)** | DDL注释(10公章/20合同章) | **是** | 申请用印时必填 |
| applied_copies | 申请份数 | 数值 | 否(自由输入) | — | **是** | NOT NULL |
| actual_copies | 实际盖章份数 | 数值 | 否(自由输入) | — | 否 | 审批时填 |
| applicant_id | 申请人 | ID外键(sys_user) | 系统自动 | — | 系统 | =当前用户 |
| apply_remark | 申请备注 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| status | 审批状态 | 枚举值 | 系统自动 | 0审批中/10通过/20驳回 | 系统 | 默认0审批中 |
| approver_id | 审批人 | ID外键(sys_user) | 否(系统按角色) | sys_user | 否 | 审批时填 |
| approved_at / approve_remark | 审批时间/备注 | 系统填充/自由文本 | 系统自动 | — | 否 | 审批时 |
| organization_id 等 | 系统字段 | 系统填充 | 系统自动 | — | 系统 | 同主表 |

**合同「需 fetch 的下拉（回存 ID）」**：business_entity_id、related_party_id、order_id（3 个外键，无字典列）。
**合同「静态枚举下拉」**：contract_direction、contract_type、related_party_type、seal_type（4 个）。

---

## 4. 场景三：新建上下游资源（ad_resource）

| 字段(列名) | 中文含义 | 存储方式 | 应选下拉? | 下拉数据来源 | 必填? | 备注 |
|------------|----------|----------|-----------|--------------|-------|------|
| id | 主键 | 系统填充 | 系统自动 | — | 系统 | UUID |
| resource_type | 资源类型 | 枚举值 | **是(静态枚举)** | ResourceType(10上游代理/20下游客户) | **是** | NOT NULL |
| name | 名称 | 自由文本 | 否(自由输入) | — | **是** | NOT NULL |
| media_type | 类型 | 字典code | **是(需fetch数据)** | ad_dict(dict_code=media_type) | 否 | 资源类型=时填 |
| channel | 渠道 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| rate_card | 刊例价 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| discount_policy | 折扣政策 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| credit_code | 信用代码 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| signing_entity | 签约主体 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| business_entity_id | 归属业务主体 | ID外键 | **是(需fetch数据)** | ad_business_entity | 否 | 选填但建议填 |
| status | 状态 | 枚举值 | **是(静态枚举)** | DDL注释(10正常/20停用) | 系统 | 默认10正常 |
| organization_id 等 | 系统字段 | 系统填充 | 系统自动 | — | 系统 | 同前 |

**资源「需 fetch 的下拉（回存 ID）」**：business_entity_id（1 个外键）+ media_type（1 个字典）。
**资源「静态枚举下拉」**：resource_type、status（2 个）。

---

## 5. 场景四：新建客户（ad_customer，基于 v3.1 DDL）

| 字段(列名) | 中文含义 | 存储方式 | 应选下拉? | 下拉数据来源 | 必填? | 备注 |
|------------|----------|----------|-----------|--------------|-------|------|
| id | 主键 | 系统填充 | 系统自动 | — | 系统 | UUID |
| name | 客户名称 | 自由文本 | 否(自由输入) | — | **是** | NOT NULL，全局唯一 |
| brand | 品牌 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| industry_code | 行业类别 | 字典code | **是(需fetch数据)** | ad_dict(dict_code=industry) | 否 | 可空 |
| signing_entity | 签约主体 | 自由文本 | 否(自由输入) | — | 否 | 可空 |
| organization_id 等 | 系统字段 | 系统填充 | 系统自动 | — | 系统 | 同前 |

> ⚠️ **重要差异（需产品/后端确认）**：Java 领域类 `AdCustomer.java` 已包含 M6 扩展字段，但 v3.1 DDL 的 `ad_customer` 表**尚未包含**这些列。若「新建客户」表单要录入它们，需先补齐 DDL 迁移。这些字段的分类如下：

| M6 扩展字段（DDL 暂缺） | 中文含义 | 存储方式 | 应选下拉? | 下拉数据来源 | 必填? |
|--------------------------|----------|----------|-----------|--------------|-------|
| contactPerson | 联系人 | 自由文本 | 否 | — | 否 |
| contactPhone | 联系电话 | 自由文本 | 否 | — | 否 |
| email | 邮箱 | 自由文本 | 否 | — | 否 |
| address | 地址 | 自由文本 | 否 | — | 否 |
| industry | 行业 | 自由文本 | 否 | — | 否 |
| customer_level | 客户等级 | 枚举值 | **是(静态枚举)** | CustomerLevel(10VIP/20普通/30潜力) | 否 |
| status | 客户状态 | 枚举值 | **是(静态枚举)** | CustomerStatus(0活跃/10非活跃/20黑名单) | 系统(默认0活跃) |
| remark | 备注 | 自由文本 | 否 | — | 否 |

**客户（按 DDL）「需 fetch 的下拉」**：industry_code（1 个字典，无外键）。若纳入 M6 领域模型，再 +2 个枚举下拉（customer_level、status）。

---

## 6. 场景五：新建业务主体（ad_business_entity）

| 字段(列名) | 中文含义 | 存储方式 | 应选下拉? | 下拉数据来源 | 必填? | 备注 |
|------------|----------|----------|-----------|--------------|-------|------|
| id | 主键 | 系统填充 | 系统自动 | — | 系统 | UUID |
| name | 主体名称 | 自由文本 | 否(自由输入) | — | **是** | NOT NULL |
| code | 主体代码 | 自由文本 | 否(自由输入) | — | **是** | NOT NULL（如 JS/TH）|
| status | 状态 | 枚举值 | **是(静态枚举)** | BusinessEntityStatus(10启用/20停用) | 系统 | 默认10启用 |
| organization_id 等 | 系统字段 | 系统填充 | 系统自动 | — | 系统 | 同前 |

**业务主体「静态枚举下拉」**：status（1 个，无外键/字典）。

---

## 7. 总览结论

1. **五类表单合计 24 个字段应以下拉呈现**，分类如下：
   - **外键关联（回存 ID、需 fetch）＝ 7 个**：订单(business_entity_id、customer_id、upstream_agent_id) ＋ 合同(business_entity_id、related_party_id、order_id) ＋ 资源(business_entity_id)。
   - **字典（需 fetch ad_dict）＝ 3 个**：订单(industry_code) ＋ 资源(media_type) ＋ 客户(industry_code)。
   - **枚举（静态选项、无需 fetch）＝ 14 个**：订单 7 ＋ 合同 4 ＋ 资源 2 ＋ 主体 1。
   - 若把「新建客户」的 M6 领域扩展字段纳入，则再 +2 枚举下拉（customer_level、status），合计 26 个。

2. **其中「点击下拉 + fetch 数据、不能手填也不能写死」的字段 = 外键 7 ＋ 字典 3 ＝ 10 个**；枚举 14 个虽是下拉，但选项固化在前端/枚举常量中，无需调接口（仅初始化时可选一次性拉取或硬编码）。

3. **必填字段清单（NOT NULL 且无默认、需用户填写）**：
   - **订单（12 个用户输入必填）**：order_name、business_entity_id、customer_id、order_type、total_amount、rebate_mode、rebate_value、media_payable_amount、delivery_start_date、delivery_end_date、receipt_method、payment_method；（另 creator_id 由系统=当前用户填充，不计用户输入）。
   - **合同（6 个必填 + 条件）**：contract_name、business_entity_id、contract_direction、contract_type、related_party_type、related_party_id；order_id 在「单笔合同」时必填；用印子表 seal_type、applied_copies 必填。
   - **资源（2 个必填）**：name、resource_type（business_entity_id、media_type 选填但建议填；status 默认 10）。
   - **客户（1 个必填）**：name（industry_code 选填）。
   - **业务主体（2 个必填）**：name、code（status 默认 10 启用）。

4. **统一系统填充约定**（不出现在创建表单或只读）：`id`、`*_time`、`*_user`、`deleted`、`organization_id`(租户上下文)、`tenant_id`、`order_no`；订单的 `status`(默认草稿)、`invoice_status/receipt_status/media_payment_status`(默认0)、`rebate_amount/receivable_amount`(计算)、各累计金额(默认0)；合同的 `seal_status`(默认未申请)、`status`(默认生效)；用印的 `applicant_id`(=当前用户)、`status`(默认审批中)；以及存 ID 但属系统上下文的 `creator_id`、`applicant_id`、`organization_id`、`contract_id`——这些虽存 ID，**不是用户下拉**，由登录/新建上下文自动带入。

5. **待确认事项（UNCLEAR）**：
   - (a) `ad_customer` v3.1 DDL 与 `AdCustomer.java` 领域模型不一致（缺 M6 的 status/customer_level/contact 等列），新建客户表单字段范围需产品/后端先定 DDL 迁移。
   - (b) `contract_no` / `order_no` 是否系统生成还是用户录入（DDL 未标注「自动」），建议确认后标记为系统自动。
   - (c) `currency` 当前是自由文本默认 CNY，建议后续字典化。
   - (d) `rebate_mode`/`*_prepay_mode`/`related_party_type`/`seal_type`/合同 `status`/`resource.status` 无独立 Java 枚举类，按 DDL 注释静态写死即可；如后续复用频繁，建议补枚举类统一。
   - (e) `ad_dict.dict_code` 注释里出现的 `seal_type/receipt_method/payment_method` 实为 Java 枚举列，真正的字典列仅 `industry`、`media_type` 两项。
