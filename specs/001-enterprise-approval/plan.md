# 企业审批管理系统技术方案

本文档基于 `spec.md` 生成，用于指导后续任务拆分、接口设计、页面设计、测试设计、实现和代码生成。

## 1. 方案目标

本方案的目标不是描述一个抽象架构，而是把需求规格落成可实现的系统蓝图。

核心交付物包括：

- 可实现的系统分层和模块边界。
- 可追踪到 `spec.md` 的领域模型、状态机和业务规则。
- 第一版数据库实体设计。
- 第一版 API 边界。
- 第一版页面和前端模块设计。
- 权限、通知、审计、导出、超时提醒的落地方式。
- 可执行的开发阶段拆分和验收测试范围。

## 2. 需求追踪索引

| 追踪编号 | 来源章节 | 需求摘要 | 方案落点 |
| --- | --- | --- | --- |
| REQ-001 | spec 1, 2, 3 | 面向几十人小公司的 Web 审批系统 | 系统架构、部署边界、页面范围 |
| REQ-002 | spec 3.2 | 支持请假、报销、采购、加班、出差 5 类审批 | 审批类型、表单模型、流程规则 |
| REQ-003 | spec 4 | 普通员工、主管、财务、人事、总经理、管理员角色 | RBAC、数据权限、菜单权限 |
| REQ-004 | spec 5 | 组织架构、部门、岗位、员工、直属上级 | 组织模块、用户模块 |
| REQ-005 | spec 6 | 默认流程与管理员配置流程 | 流程模板、流程实例、节点规则 |
| REQ-006 | spec 7 | 同意、驳回、转交、加签、补充材料、抄送 | 审批动作服务、状态机 |
| REQ-007 | spec 8, 9 | 草稿、审批中、通过、驳回、撤回、补充材料、作废 | 申请状态机 |
| REQ-008 | spec 10 | 5 类审批表单字段 | 表单数据模型、前端动态表单 |
| REQ-009 | spec 11 | 工作台、申请、待办、已办、抄送、管理配置页面 | 前端路由和页面模块 |
| REQ-010 | spec 12 | 搜索、筛选、导出 | 查询 API、导出服务 |
| REQ-011 | spec 13 | 轻量统计看板 | 统计查询服务 |
| REQ-012 | spec 14, 15 | 系统内通知、24 小时超时提醒 | 通知模块、定时任务 |
| REQ-013 | spec 16, 17 | 审批历史与审计日志 | 时间线、审计日志模块 |
| REQ-014 | spec 19 | 第一版不做范围 | 架构边界、集成边界 |
| REQ-015 | spec 20 | 验收标准 | 测试计划、验收用例 |

## 3. 技术选型

第一版建议采用前后端分离架构。

### 3.1 前端

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- Ant Design

选择理由：

- 后台管理系统控件密集，Ant Design 可以快速覆盖表格、表单、弹窗、筛选、步骤条、时间线等场景。
- TypeScript 能约束审批类型、状态、动作和表单结构。
- TanStack Query 适合处理列表、详情、待办、通知等服务端状态。

### 3.2 后端

- Java
- Java 1.8
- Spring Boot 2.7.x
- Spring Security
- Spring Data JPA 或 MyBatis-Plus
- PostgreSQL

选择理由：

- 企业审批系统属于典型企业后台业务，Java/Spring 生态成熟，适合长期维护。
- Spring Security 适合实现登录、角色权限、接口权限、方法级授权和安全上下文。
- Spring 事务模型成熟，适合审批流、状态流转、任务生成、通知和审计日志等强一致业务。
- Spring Boot 适合按业务模块组织 Controller、Application Service、Domain Service、Repository/Mapper。
- Java 类型系统稳定，适合沉淀审批状态机、流程规则、权限判断等复杂业务规则。
- PostgreSQL 支持结构化字段、JSON 字段、事务、索引和复杂查询，适合审批表单和历史追踪。
- Spring Boot 2.7.x 可兼容 Java 1.8；不得升级到要求 Java 17 的 Spring Boot 3.x。

数据访问层默认建议：

- 如果优先追求领域模型表达和对象关系映射，使用 Spring Data JPA。
- 如果优先追求 SQL 可控性和复杂查询可读性，使用 MyBatis-Plus。
- 审批列表、统计、导出等复杂查询可以使用自定义 SQL，即使主数据访问方案选择 JPA。

### 3.3 文件存储

第一版使用本地文件存储，保存上传附件。

附件元数据写入数据库，文件本体保存到服务端指定目录。

后续可以替换为对象存储，但第一版不引入第三方存储服务。

### 3.4 数据库迁移

第一版使用 Flyway 管理数据库结构迁移和基础字典数据。

迁移脚本应覆盖：

- 用户、组织、角色、权限基础表。
- 审批申请、流程、任务、附件、通知、审计日志表。
- 内置角色、权限点、默认流程模板等初始化数据。

数据库物理表名按业务域使用前缀：

- `sys_`：系统、账号、角色、权限相关表，例如 `sys_user`、`sys_role`、`sys_permission`。
- `org_`：组织架构相关表，例如 `org_department`、`org_position`。
- `approval_`：审批业务相关表，例如 `approval_request`、`approval_task`、`approval_attachment`。
- `audit_`：审计相关表，例如 `audit_log`。

所有业务表必须支持软删除，不得通过物理删除移除业务数据。

### 3.5 定时任务

第一版在后端服务中实现定时任务，用于扫描超过 24 小时未处理的审批节点并创建系统内通知。

使用 Spring Scheduling 实现定时扫描。

不引入独立任务队列，避免过早复杂化。

### 3.6 认证方式

第一版使用账号密码登录 + 服务端签发访问令牌。

令牌用于识别当前用户、角色和权限。

后端使用 Spring Security 统一处理认证、鉴权、当前用户上下文和接口访问控制。

第一版不做单点登录、短信登录、第三方登录。

### 3.7 测试框架

后端测试优先使用：

- JUnit 5
- Spring Boot Test
- Mockito
- Testcontainers PostgreSQL

审批状态机、流程解析和数据权限判断应优先写单元测试；涉及数据库事务、流程实例、通知和审计的场景应写集成测试。

## 4. 总体架构

```text
Web Browser
  |
  | HTTP JSON API
  v
Frontend React App
  |
  | REST API
  v
Backend Spring Boot App
  |
  +-- Controller Layer
  +-- Application Service Layer
  +-- Domain Service Layer
  +-- Repository / Mapper Layer
  +-- Security Layer
  |
  +-- auth
  +-- user
  +-- organization
  +-- rbac
  +-- approval
  +-- workflow
  +-- notification
  +-- audit
  +-- dashboard
  +-- export
  |
  v
PostgreSQL

Local File Storage
```

说明：

- Controller 只处理 HTTP 入参、出参和状态码。
- Application Service 编排一个完整业务用例，例如提交申请、审批同意、流程配置保存。
- Domain Service 承载状态机、流程解析、审批人解析、数据权限判断等核心规则。
- Repository/Mapper 负责数据库访问。
- Security 负责登录、令牌校验、权限注解、当前用户上下文。

## 5. 后端模块划分

后端采用 Spring Boot 单体模块化架构。第一版不拆微服务，避免为几十人规模的小公司审批系统引入过高运维复杂度。

建议基础包结构：

```text
com.company.approval
  common
    config
    exception
    response
    pagination
    storage
  security
    config
    jwt
    principal
    permission
  auth
    controller
    application
    dto
  user
    controller
    application
    domain
    repository
    dto
  organization
    controller
    application
    domain
    repository
    dto
  rbac
    controller
    application
    domain
    repository
    dto
  approval
    controller
    application
    domain
    repository
    dto
  workflow
    controller
    application
    domain
    repository
    dto
  notification
    controller
    application
    domain
    repository
    dto
  audit
    controller
    application
    domain
    repository
    dto
  dashboard
    controller
    application
    dto
  export
    controller
    application
```

分层约束：

- Controller 不直接操作 Repository。
- Controller 不承载审批状态流转、流程推进、权限判断等业务规则。
- Application Service 负责事务边界和业务用例编排。
- Domain Service 负责可复用核心规则，例如状态机、审批人解析、数据权限判断。
- Repository/Mapper 只负责数据访问。
- DTO 与 Entity 分离，避免直接把数据库实体暴露给前端。

### 5.1 Auth 模块

覆盖需求：REQ-003、REQ-013

职责：

- 用户登录。
- 当前用户信息查询。
- 登录审计记录。
- Spring Security 认证过滤器。
- 当前用户上下文。

主要能力：

- `POST /auth/login`
- `GET /auth/me`
- `POST /auth/logout`

建议类：

- `AuthController`
- `AuthApplicationService`
- `JwtTokenService`
- `CurrentUserProvider`
- `SecurityConfig`
- `JwtAuthenticationFilter`

### 5.2 Organization 模块

覆盖需求：REQ-004

职责：

- 公司、部门、岗位、员工组织关系维护。
- 直属上级维护。
- 为流程引擎提供直属主管解析能力。
- 为数据权限提供部门范围判断。

主要能力：

- 部门树查询。
- 部门新增、编辑、停用。
- 岗位新增、编辑、停用。
- 员工组织归属维护。
- 直属上级设置。

### 5.3 User 模块

覆盖需求：REQ-003、REQ-004

职责：

- 用户账号管理。
- 用户状态管理。
- 用户角色分配。
- 用户所属部门和岗位维护。

主要能力：

- 用户列表。
- 用户详情。
- 新增用户。
- 编辑用户。
- 停用用户。
- 分配角色。

### 5.4 RBAC 模块

覆盖需求：REQ-003、REQ-009

职责：

- 角色管理。
- 权限点管理。
- 菜单可见性控制。
- 操作权限控制。
- 数据权限策略判断。

第一版内置角色：

- employee
- supervisor
- finance
- hr
- general_manager
- admin

权限分三类：

- 菜单权限：能看到哪些页面。
- 操作权限：能执行哪些动作。
- 数据权限：能看哪些申请记录。

Spring Security 落地方式：

- 接口级权限使用 `@PreAuthorize` 或自定义权限注解。
- 当前用户角色和权限从安全上下文读取。
- 数据权限不只依赖注解，必须在查询服务中拼接数据范围条件。
- 审批动作必须同时校验操作权限和当前任务处理人身份。

### 5.5 Approval 模块

覆盖需求：REQ-002、REQ-006、REQ-007、REQ-008、REQ-010、REQ-013

职责：

- 审批申请创建、保存草稿、提交。
- 审批详情、列表、搜索、筛选。
- 审批动作处理。
- 审批历史时间线。
- 申请撤回、重新提交、作废。

核心聚合：

- ApprovalRequest：审批申请主记录。
- ApprovalFormData：审批表单业务字段。
- ApprovalTask：当前或历史审批任务。
- ApprovalActionLog：审批动作日志。
- ApprovalAttachment：附件。

建议类：

- `ApprovalController`
- `ApprovalApplicationService`
- `ApprovalCommandService`
- `ApprovalQueryService`
- `ApprovalStateMachine`
- `ApprovalPermissionService`
- `ApprovalFormValidator`
- `ApprovalRequestRepository`
- `ApprovalTaskRepository`
- `ApprovalActionLogRepository`
- `ApprovalAttachmentRepository`

### 5.6 Workflow 模块

覆盖需求：REQ-005、REQ-006

职责：

- 默认流程定义。
- 管理员配置流程。
- 根据申请类型、金额、部门解析流程。
- 创建流程实例。
- 推进流程节点。
- 处理转交、加签、抄送、补充材料。

第一版不实现 BPMN 设计器。

流程配置使用结构化节点列表实现。

建议类：

- `WorkflowTemplateController`
- `WorkflowApplicationService`
- `WorkflowResolver`
- `WorkflowInstanceService`
- `ApproverResolver`
- `WorkflowTemplateRepository`
- `WorkflowNodeTemplateRepository`
- `WorkflowInstanceRepository`
- `WorkflowNodeInstanceRepository`

### 5.7 Notification 模块

覆盖需求：REQ-012

职责：

- 创建系统内通知。
- 查询通知列表。
- 标记已读。
- 生成待办、审批结果、转交、加签、抄送、超时、流程结束通知。

建议类：

- `NotificationController`
- `NotificationApplicationService`
- `NotificationService`
- `OverdueTaskScheduler`
- `NotificationRepository`

### 5.8 Audit 模块

覆盖需求：REQ-013

职责：

- 记录登录、申请、审批、组织、用户、角色、流程配置等关键操作。
- 提供审计日志查询。
- 统一封装审计写入接口，供业务模块调用。

建议类：

- `AuditLogController`
- `AuditLogApplicationService`
- `AuditLogService`
- `AuditLogRepository`

### 5.9 Dashboard 模块

覆盖需求：REQ-009、REQ-011、REQ-012

职责：

- 工作台数据聚合。
- 统计看板查询。
- 待办数量、申请进度、最近动态、超时提醒。
- 各类型审批数量、通过驳回数量、平均耗时、超时数量。

### 5.10 Export 模块

覆盖需求：REQ-010

职责：

- 管理员和总经理导出审批记录。
- 支持列表筛选条件导出。
- 第一版导出 CSV 或 XLSX。

## 6. 前端模块划分

### 6.1 路由结构

覆盖需求：REQ-009

```text
/login
/dashboard
/approvals/new
/approvals/my
/approvals/todo
/approvals/done
/approvals/cc
/approvals/manage
/approvals/:id
/organization
/users
/roles
/workflow-config
/notifications
/audit-logs
```

### 6.2 页面与组件

#### 工作台

覆盖需求：REQ-009、REQ-011、REQ-012

展示：

- 待办数量卡片。
- 我的申请进度列表。
- 最近审批动态。
- 超时提醒。
- 快速发起入口。

#### 发起申请

覆盖需求：REQ-002、REQ-008

组件：

- 审批类型选择器。
- 通用字段区。
- 动态表单区。
- 附件上传。
- 草稿保存按钮。
- 提交按钮。

表单根据审批类型切换字段。

#### 我的申请

覆盖需求：REQ-007、REQ-010、REQ-013

展示当前用户提交的申请。

支持：

- 搜索筛选。
- 查看详情。
- 撤回。
- 重新提交。
- 补充材料。

#### 我的待办

覆盖需求：REQ-006、REQ-009

展示当前用户待处理任务。

支持进入详情执行：

- 同意。
- 驳回。
- 转交。
- 加签。
- 要求补充材料。
- 抄送。

#### 我的已办

覆盖需求：REQ-009、REQ-013

展示当前用户处理过的任务。

#### 抄送我的

覆盖需求：REQ-006、REQ-009、REQ-012

展示抄送给当前用户的申请。

#### 审批详情

覆盖需求：REQ-006、REQ-007、REQ-013

详情页是核心页面，应包含：

- 基础信息。
- 表单字段。
- 附件。
- 当前状态。
- 当前节点。
- 可执行动作。
- 审批时间线。
- 抄送信息。
- 超时标记。

#### 审批管理

覆盖需求：REQ-003、REQ-010

按角色显示权限范围内的申请记录。

支持筛选、查看、导出。

#### 组织架构

覆盖需求：REQ-004

左侧部门树，右侧部门人员列表。

支持维护部门、岗位、员工、直属上级。

#### 用户管理

覆盖需求：REQ-003、REQ-004

用户列表、用户详情、角色分配、状态管理。

#### 角色权限

覆盖需求：REQ-003

角色列表、权限配置、菜单权限、操作权限。

#### 流程配置

覆盖需求：REQ-005

按审批类型配置节点：

- 节点顺序。
- 审批人规则。
- 金额条件。
- 部门条件。
- 抄送规则。
- 启用状态。

#### 通知中心

覆盖需求：REQ-012

展示系统内通知，支持已读、未读筛选。

#### 审计日志

覆盖需求：REQ-013

展示关键操作记录，支持按操作人、类型、时间筛选。

## 7. 核心领域模型

本节描述数据库实体和领域对象的第一版结构。Spring Boot 实现时应将实体、DTO、请求对象、响应对象分离。

建议规则：

- Entity 对应数据库表。
- DTO 用于接口输入输出。
- Enum 用于审批类型、状态、动作、节点类型、任务状态、通知类型。
- 金额使用 `BigDecimal`。
- 时间使用 `LocalDate`、`LocalDateTime`。
- JSON 表单字段在 PostgreSQL 中使用 `jsonb`，Java 中可用 `String`、`JsonNode` 或自定义转换器承载。
- 核心写操作必须使用 `@Transactional`。

### 7.1 用户与组织

#### User

字段：

- id
- username
- passwordHash
- displayName
- phone
- email
- status
- departmentId
- positionId
- managerId
- createdAt
- updatedAt

#### Department

字段：

- id
- name
- parentId
- sortOrder
- status
- createdAt
- updatedAt

#### Position

字段：

- id
- name
- departmentId
- status
- createdAt
- updatedAt

#### Role

字段：

- id
- code
- name
- description
- builtIn
- createdAt
- updatedAt

#### UserRole

字段：

- userId
- roleId

#### Permission

字段：

- id
- code
- name
- type
- description

#### RolePermission

字段：

- roleId
- permissionId

### 7.2 审批申请

#### ApprovalRequest

字段：

- id
- requestNo
- type
- title
- applicantId
- departmentId
- urgency
- status
- formData
- currentNodeInstanceId
- submittedAt
- completedAt
- voidedAt
- voidedBy
- voidReason
- createdAt
- updatedAt

说明：

- `type` 枚举：leave、reimbursement、purchase、overtime、business_trip。
- `urgency` 枚举：normal、urgent。
- `status` 枚举：draft、in_progress、approved、rejected、withdrawn、need_more_info、voided。
- `formData` 使用 JSON 字段保存不同审批类型的业务字段。
- 常用筛选字段如金额、开始时间、结束时间可以同步冗余到独立列，方便查询和统计。

建议冗余字段：

- amount
- startDate
- endDate

### 7.3 附件

#### ApprovalAttachment

字段：

- id
- approvalRequestId
- uploaderId
- fileName
- filePath
- fileSize
- mimeType
- purpose
- createdAt

### 7.4 流程定义

#### WorkflowTemplate

字段：

- id
- approvalType
- name
- enabled
- isDefault
- departmentId
- createdBy
- createdAt
- updatedAt

#### WorkflowNodeTemplate

字段：

- id
- workflowTemplateId
- nodeName
- nodeType
- approverRuleType
- approverUserId
- approverRoleCode
- amountOperator
- amountValue
- sortOrder
- required
- createdAt
- updatedAt

说明：

- `nodeType`：approval、confirm、cc。
- `approverRuleType`：direct_manager、role、specific_user、general_manager。
- 金额条件用于报销和采购流程。

### 7.5 流程实例与任务

#### WorkflowInstance

字段：

- id
- approvalRequestId
- workflowTemplateId
- status
- startedAt
- completedAt
- createdAt
- updatedAt

#### WorkflowNodeInstance

字段：

- id
- workflowInstanceId
- nodeTemplateId
- nodeName
- nodeType
- status
- sortOrder
- startedAt
- completedAt
- createdAt
- updatedAt

状态：

- pending
- active
- approved
- rejected
- transferred
- added
- skipped
- cancelled

#### ApprovalTask

字段：

- id
- approvalRequestId
- workflowNodeInstanceId
- assigneeId
- sourceAssigneeId
- status
- assignedAt
- actedAt
- dueAt
- overdue
- createdAt
- updatedAt

状态：

- pending
- completed
- transferred
- cancelled

### 7.6 审批动作与历史

#### ApprovalActionLog

字段：

- id
- approvalRequestId
- workflowNodeInstanceId
- taskId
- actorId
- action
- comment
- metadata
- createdAt

动作：

- create
- save_draft
- submit
- approve
- reject
- withdraw
- transfer
- add_approver
- request_more_info
- resubmit
- cc
- void

### 7.7 抄送

#### ApprovalCc

字段：

- id
- approvalRequestId
- userId
- createdBy
- createdAt
- readAt

### 7.8 通知

#### Notification

字段：

- id
- receiverId
- type
- title
- content
- relatedApprovalRequestId
- readAt
- createdAt

类型：

- todo_created
- approved
- rejected
- need_more_info
- transferred
- added
- cc
- overdue
- completed

### 7.9 审计日志

#### AuditLog

字段：

- id
- actorId
- action
- targetType
- targetId
- result
- description
- ip
- userAgent
- createdAt

## 8. 审批状态机

覆盖需求：REQ-006、REQ-007

### 8.1 状态流转

```text
draft
  | submit
  v
in_progress
  | approve last node
  v
approved

in_progress
  | reject
  v
rejected
  | resubmit
  v
in_progress

in_progress
  | request_more_info
  v
need_more_info
  | resubmit
  v
in_progress

in_progress
  | withdraw
  v
withdrawn
  | edit and submit
  v
in_progress

approved
  | admin void
  v
voided
```

### 8.2 操作约束

| 当前状态 | 允许操作 | 操作人 |
| --- | --- | --- |
| draft | 编辑、保存草稿、提交 | 申请人 |
| in_progress | 撤回 | 申请人 |
| in_progress | 同意、驳回、转交、加签、补充材料、抄送 | 当前审批任务处理人 |
| rejected | 修改、重新提交 | 申请人 |
| need_more_info | 补充材料、重新提交 | 申请人 |
| approved | 作废 | 系统管理员 |
| withdrawn | 修改、重新提交 | 申请人 |
| voided | 查看 | 有权限用户 |

### 8.3 关键规则

- 提交后不能直接修改，必须撤回、驳回或补充材料后才能编辑。
- 已通过申请不能由申请人撤回。
- 作废必须填写作废原因。
- 审批意见必须写入审批历史。
- 审批动作必须同时写入审计日志。

## 9. 默认流程规则

覆盖需求：REQ-005

### 9.1 请假

节点：

1. 直属主管审批
2. 人事确认

### 9.2 报销

节点：

1. 直属主管审批
2. 财务审批
3. 金额超过 1000 元时，总经理审批

### 9.3 采购

节点：

1. 直属主管审批
2. 总经理审批

采购默认经过总经理审批。金额超过 5000 元时仍必须经过总经理审批；如果管理员配置了其他流程，该条件不能绕过总经理审批。

### 9.4 加班

节点：

1. 直属主管审批

### 9.5 出差

节点：

1. 直属主管审批
2. 总经理审批

## 10. 表单校验规则

覆盖需求：REQ-008

### 10.1 通用规则

- 申请类型必填。
- 申请人由当前登录用户自动带出。
- 所属部门由当前用户组织信息自动带出。
- 紧急程度必填，默认普通。
- 提交时间由系统生成。
- 草稿可以缺少部分必填业务字段。
- 提交时必须满足对应审批类型的必填规则。

### 10.2 请假

- 请假类型必填。
- 开始时间必填。
- 结束时间必填。
- 结束时间必须晚于开始时间。
- 请假时长由系统自动计算。
- 请假原因必填。
- 附件可选。

### 10.3 报销

- 报销类型必填。
- 报销金额必填，且必须大于 0。
- 费用发生日期必填。
- 费用说明必填。
- 收款账户必填。
- 附件必填。
- 可关联出差申请。

### 10.4 采购

- 采购物品名称必填。
- 采购数量必填，且必须大于 0。
- 预计金额必填，且必须大于 0。
- 供应商名称可选。
- 采购原因必填。
- 期望到货日期必填。
- 附件必填。

### 10.5 加班

- 加班日期必填。
- 开始时间必填。
- 结束时间必填。
- 结束时间必须晚于开始时间。
- 加班时长由系统自动计算。
- 加班原因必填。
- 是否申请调休必填。
- 附件可选。

### 10.6 出差

- 出差地点必填。
- 开始日期必填。
- 结束日期必填。
- 结束日期不得早于开始日期。
- 出差天数由系统自动计算。
- 出差事由必填。
- 预计费用必填，且不能小于 0。
- 交通方式必填。
- 同行人员可选。
- 附件可选。

## 11. 数据权限设计

覆盖需求：REQ-003、REQ-010

### 11.1 数据可见性规则

| 角色 | 可见数据 |
| --- | --- |
| 普通员工 | 自己提交的、自己审批过的、抄送给自己的 |
| 部门主管 | 普通员工范围 + 本部门员工提交的 |
| 财务人员 | 普通员工范围 + 报销相关审批记录 |
| 人事人员 | 普通员工范围 + 请假和加班相关审批记录 |
| 总经理/老板 | 全公司审批记录 |
| 系统管理员 | 全公司审批记录、配置和审计数据 |

### 11.2 后端控制方式

- 所有列表查询必须经过数据权限过滤。
- 所有详情查询必须检查当前用户是否具备查看该申请的权限。
- 所有审批动作必须检查当前用户是否是当前有效任务的处理人。
- 管理员作废必须检查 admin 角色和申请状态。

### 11.3 前端控制方式

- 前端根据 `/auth/me` 返回的菜单权限渲染侧边栏。
- 前端根据详情接口返回的 `availableActions` 渲染操作按钮。
- 前端隐藏按钮不代表授权，所有授权以服务端为准。

## 12. API 设计

### 12.1 统一响应结构

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

错误响应：

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "APPROVAL_NOT_FOUND",
    "message": "审批申请不存在"
  }
}
```

### 12.2 Auth API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录 |
| GET | `/api/auth/me` | 当前用户、角色、权限 |
| POST | `/api/auth/logout` | 登出 |

### 12.3 Approval API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/approvals/drafts` | 创建或保存草稿 |
| POST | `/api/approvals` | 提交申请 |
| GET | `/api/approvals/my` | 我的申请 |
| GET | `/api/approvals/todo` | 我的待办 |
| GET | `/api/approvals/done` | 我的已办 |
| GET | `/api/approvals/cc` | 抄送我的 |
| GET | `/api/approvals/manage` | 审批管理列表 |
| GET | `/api/approvals/:id` | 审批详情 |
| POST | `/api/approvals/:id/submit` | 草稿提交或重新提交 |
| POST | `/api/approvals/:id/withdraw` | 撤回 |
| POST | `/api/approvals/:id/approve` | 同意 |
| POST | `/api/approvals/:id/reject` | 驳回 |
| POST | `/api/approvals/:id/transfer` | 转交 |
| POST | `/api/approvals/:id/add-approver` | 加签 |
| POST | `/api/approvals/:id/request-more-info` | 要求补充材料 |
| POST | `/api/approvals/:id/cc` | 抄送 |
| POST | `/api/approvals/:id/void` | 作废 |
| GET | `/api/approvals/export` | 导出审批记录 |

### 12.4 Workflow API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/workflow-templates` | 流程配置列表 |
| GET | `/api/workflow-templates/:id` | 流程配置详情 |
| POST | `/api/workflow-templates` | 新建流程配置 |
| PUT | `/api/workflow-templates/:id` | 更新流程配置 |
| POST | `/api/workflow-templates/:id/enable` | 启用 |
| POST | `/api/workflow-templates/:id/disable` | 停用 |

### 12.5 Organization API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/departments/tree` | 部门树 |
| POST | `/api/departments` | 新增部门 |
| PUT | `/api/departments/:id` | 编辑部门 |
| POST | `/api/departments/:id/disable` | 停用部门 |
| GET | `/api/positions` | 岗位列表 |
| POST | `/api/positions` | 新增岗位 |
| PUT | `/api/positions/:id` | 编辑岗位 |

### 12.6 User and Role API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/users` | 用户列表 |
| POST | `/api/users` | 新增用户 |
| GET | `/api/users/:id` | 用户详情 |
| PUT | `/api/users/:id` | 编辑用户 |
| POST | `/api/users/:id/disable` | 停用用户 |
| PUT | `/api/users/:id/roles` | 分配角色 |
| GET | `/api/roles` | 角色列表 |
| PUT | `/api/roles/:id/permissions` | 更新角色权限 |

### 12.7 Notification API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/notifications` | 通知列表 |
| POST | `/api/notifications/:id/read` | 标记单条已读 |
| POST | `/api/notifications/read-all` | 全部标记已读 |

### 12.8 Dashboard API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/dashboard/workbench` | 工作台数据 |
| GET | `/api/dashboard/statistics` | 统计看板 |

### 12.9 Audit API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/audit-logs` | 审计日志列表 |

## 13. 关键业务服务设计

本节业务用例应主要落在 Application Service，并通过 `@Transactional` 保证一次业务操作内的状态变更、任务变更、动作日志、通知和审计日志一致提交。

原则：

- 查询类服务默认只读事务。
- 提交、审批、撤回、作废、流程配置变更必须使用写事务。
- 审批动作失败时，不能留下半完成的任务、通知或审计记录。
- 审计日志可记录失败操作，但应由统一异常处理和审计组件明确处理，避免吞掉业务异常。

### 13.1 提交流程

覆盖需求：REQ-005、REQ-007、REQ-008、REQ-012、REQ-013

步骤：

1. 校验申请表单。
2. 创建或更新 ApprovalRequest。
3. 根据审批类型、部门、金额解析 WorkflowTemplate。
4. 创建 WorkflowInstance。
5. 创建首个 WorkflowNodeInstance。
6. 解析首个节点审批人。
7. 创建 ApprovalTask。
8. 将申请状态置为 in_progress。
9. 写入 ApprovalActionLog。
10. 写入 AuditLog。
11. 创建待办通知。

### 13.2 同意流程

步骤：

1. 校验当前用户是否是当前任务处理人。
2. 将当前 ApprovalTask 标记为 completed。
3. 写入 approve 动作日志。
4. 判断当前节点是否完成。
5. 如果还有下一节点，创建下一节点任务并通知审批人。
6. 如果没有下一节点，将申请状态置为 approved。
7. 写入流程结束通知。
8. 写入审计日志。

### 13.3 驳回流程

步骤：

1. 校验当前用户是否是当前任务处理人。
2. 将当前任务标记为 completed。
3. 将申请状态置为 rejected。
4. 取消后续未处理任务。
5. 通知申请人。
6. 写入动作日志和审计日志。

### 13.4 补充材料流程

步骤：

1. 校验当前用户是否是当前任务处理人。
2. 将申请状态置为 need_more_info。
3. 暂停当前节点任务。
4. 通知申请人补充材料。
5. 写入动作日志和审计日志。

申请人重新提交后：

1. 校验状态为 need_more_info。
2. 更新表单和附件。
3. 将状态恢复为 in_progress。
4. 重新激活当前审批任务。
5. 通知审批人。

### 13.5 转交流程

步骤：

1. 校验当前用户是否是当前任务处理人。
2. 校验目标用户有效。
3. 原任务状态置为 transferred。
4. 创建新任务并指派给目标用户。
5. 通知目标用户。
6. 写入动作日志和审计日志。

### 13.6 加签流程

步骤：

1. 校验当前用户是否是当前任务处理人。
2. 校验加签人有效。
3. 在当前节点后插入加签节点实例，或在当前节点内追加加签任务。
4. 通知加签人。
5. 写入动作日志和审计日志。

第一版建议采用“当前节点后插入加签节点实例”的方式，规则更清晰。

### 13.7 抄送流程

步骤：

1. 校验当前用户是否有该申请操作权限。
2. 创建 ApprovalCc 记录。
3. 创建抄送通知。
4. 写入动作日志和审计日志。

### 13.8 撤回流程

步骤：

1. 校验当前用户是申请人。
2. 校验申请状态为 in_progress。
3. 将申请状态置为 withdrawn。
4. 取消当前和后续未完成任务。
5. 写入动作日志和审计日志。

### 13.9 作废流程

步骤：

1. 校验当前用户是系统管理员。
2. 校验申请状态为 approved。
3. 校验作废原因非空。
4. 将申请状态置为 voided。
5. 写入作废人、作废时间、作废原因。
6. 写入动作日志和审计日志。

## 14. 查询、筛选和导出

覆盖需求：REQ-010

### 14.1 通用筛选参数

审批列表支持：

- approvalType
- status
- applicantId
- departmentId
- startDate
- endDate
- urgency
- keyword
- page
- pageSize

报销和采购额外支持：

- minAmount
- maxAmount

### 14.2 导出规则

- 只有管理员和总经理可以导出。
- 导出结果必须受当前筛选条件影响。
- 导出字段包含申请编号、类型、申请人、部门、紧急程度、状态、金额、提交时间、完成时间、当前节点。
- 导出操作写入审计日志。

## 15. 通知和超时提醒

覆盖需求：REQ-012

### 15.1 通知创建规则

以下事件创建通知：

- 新待办。
- 同意。
- 驳回。
- 要求补充材料。
- 转交。
- 加签。
- 抄送。
- 超时。
- 流程结束。

### 15.2 超时扫描规则

定时任务每 30 分钟执行一次。

扫描条件：

- ApprovalTask.status = pending
- dueAt <= 当前时间
- overdue = false

处理：

- 将任务标记为 overdue。
- 创建 overdue 通知。
- 在审批详情时间线中展示超时标记。

任务创建时，`dueAt = assignedAt + 24 小时`。

## 16. 审计策略

覆盖需求：REQ-013

### 16.1 必须审计的操作

- 登录。
- 创建申请。
- 保存草稿。
- 提交申请。
- 撤回申请。
- 同意审批。
- 驳回审批。
- 转交审批。
- 加签审批。
- 要求补充材料。
- 重新提交。
- 作废申请。
- 修改组织架构。
- 修改用户。
- 修改角色。
- 修改权限。
- 修改流程配置。
- 导出审批记录。

### 16.2 审计写入原则

- 业务操作成功后必须写审计日志。
- 关键失败操作也应写审计日志，例如无权限操作、状态不允许操作。
- 审计日志不允许普通用户修改或删除。

## 17. 页面到 API 映射

| 页面 | 主要 API |
| --- | --- |
| 登录 | `/api/auth/login` |
| 工作台 | `/api/dashboard/workbench` |
| 发起申请 | `/api/approvals/drafts`, `/api/approvals`, 附件上传 |
| 我的申请 | `/api/approvals/my`, `/api/approvals/:id` |
| 我的待办 | `/api/approvals/todo`, 审批动作 API |
| 我的已办 | `/api/approvals/done` |
| 抄送我的 | `/api/approvals/cc` |
| 审批详情 | `/api/approvals/:id` |
| 审批管理 | `/api/approvals/manage`, `/api/approvals/export` |
| 组织架构 | `/api/departments/tree`, `/api/positions`, `/api/users` |
| 用户管理 | `/api/users`, `/api/roles` |
| 角色权限 | `/api/roles`, `/api/roles/:id/permissions` |
| 流程配置 | `/api/workflow-templates` |
| 通知中心 | `/api/notifications` |
| 审计日志 | `/api/audit-logs` |

## 18. 权限点清单

覆盖需求：REQ-003、REQ-009

### 18.1 菜单权限

- menu.dashboard
- menu.approvals.new
- menu.approvals.my
- menu.approvals.todo
- menu.approvals.done
- menu.approvals.cc
- menu.approvals.manage
- menu.organization
- menu.users
- menu.roles
- menu.workflow_config
- menu.notifications
- menu.audit_logs

### 18.2 操作权限

- approval.create
- approval.save_draft
- approval.submit
- approval.view
- approval.withdraw
- approval.approve
- approval.reject
- approval.transfer
- approval.add_approver
- approval.request_more_info
- approval.cc
- approval.void
- approval.export
- organization.manage
- user.manage
- role.manage
- workflow.manage
- audit.view
- dashboard.statistics.view

## 19. 非功能要求

### 19.1 性能

- 常规列表查询在 2 秒内返回。
- 工作台数据在 2 秒内返回。
- 单页列表默认分页 20 条。
- 审计日志、审批列表必须分页查询。

### 19.2 安全

- 密码必须哈希存储。
- 使用 Spring Security 统一认证与授权。
- 使用 JWT 或等价访问令牌承载登录态。
- 所有业务 API 必须认证。
- 所有管理 API 必须校验角色和权限。
- 关键接口使用 `@PreAuthorize` 或统一权限校验组件。
- 附件下载必须校验当前用户是否有申请查看权限。
- 服务端必须进行数据权限判断。

### 19.3 可维护性

- 审批类型、状态、动作使用枚举。
- 表单字段使用类型定义和校验规则集中维护。
- 流程推进逻辑集中在 WorkflowApplicationService 和 WorkflowInstanceService。
- 审计和通知通过统一服务写入。

### 19.4 可扩展性

- 新增审批类型时，优先新增表单定义和流程配置，不改核心状态机。
- 后续可替换本地文件存储为对象存储。
- 后续可新增邮件、短信、企业微信、钉钉通知渠道。

## 20. 第一版实施阶段

### 阶段 1：项目骨架和基础设施

目标：

- 建立 Spring Boot 后端工程和 React 前端工程。
- 建立 PostgreSQL 连接和 Flyway 迁移。
- 建立统一响应、异常处理、分页、日志基础设施。
- 建立 Spring Security、JWT 和当前用户上下文。
- 建立认证、用户、角色基础模型。
- 建立基础布局和登录页面。

产出：

- 可登录的系统。
- 基础用户、角色、权限数据。
- 可运行的数据库迁移脚本。

### 阶段 2：组织、用户、角色权限

目标：

- 实现组织架构。
- 实现用户管理。
- 实现角色权限。
- 实现菜单权限和操作权限。

产出：

- 管理员可维护组织、用户、角色。
- 不同角色看到不同菜单。

### 阶段 3：审批申请和表单

目标：

- 实现 5 类审批表单。
- 实现保存草稿、提交申请。
- 实现附件上传。
- 实现我的申请和审批详情。

产出：

- 员工可以创建草稿并提交申请。

### 阶段 4：流程引擎和审批动作

目标：

- 实现默认流程。
- 实现流程实例和审批任务。
- 实现同意、驳回、撤回、补充材料、转交、加签、抄送。
- 实现审批时间线。

产出：

- 审批可以按默认流程完整流转。

### 阶段 5：流程配置

目标：

- 实现流程配置页面和 API。
- 支持按类型、部门、金额配置节点。
- 支持审批人规则。

产出：

- 管理员可以配置审批流程。

### 阶段 6：通知、超时、审计

目标：

- 实现通知中心。
- 实现超时扫描。
- 实现审计日志。

产出：

- 关键事件可通知。
- 关键操作可追溯。

### 阶段 7：管理列表、统计和导出

目标：

- 实现审批管理。
- 实现筛选和导出。
- 实现工作台和统计看板。

产出：

- 管理者可查看数据、筛选数据、导出数据。

### 阶段 8：验收和收口

目标：

- 按 `spec.md` 第 20 章逐项验收。
- 补齐异常场景测试。
- 修正权限、状态流转和审计遗漏。

产出：

- 满足第一版验收标准的系统。

## 21. 测试计划

覆盖需求：REQ-015

后端测试默认工具：

- JUnit 5
- Spring Boot Test
- Mockito
- Testcontainers PostgreSQL

前端测试可使用：

- Vitest
- React Testing Library
- Playwright

### 21.1 单元测试

重点覆盖：

- 表单校验。
- 审批状态机。
- 流程解析。
- 数据权限判断。
- 超时判断。

### 21.2 集成测试

重点覆盖：

- 提交申请创建流程实例。
- 同意后流转到下一节点。
- 驳回后回到申请人。
- 补充材料后重新提交。
- 转交后新审批人收到待办。
- 加签后新增节点。
- 作废已通过申请。
- 审计日志写入。
- 通知写入。

### 21.3 端到端测试

核心场景：

- 员工提交请假，主管同意，人事确认，流程通过。
- 员工提交 1200 元报销，主管同意，财务同意，总经理同意，流程通过。
- 员工提交采购，主管驳回，员工修改后重新提交。
- 员工提交加班，主管要求补充材料，员工补充后通过。
- 员工提交出差，主管转交给另一主管处理。
- 管理员作废已通过申请。
- 总经理导出审批记录。

### 21.4 权限测试

必须验证：

- 普通员工不能查看他人申请。
- 主管只能查看本部门申请。
- 财务只能查看报销相关审批记录。
- 人事只能查看请假、加班相关记录。
- 总经理可以查看全公司。
- 管理员可以查看审计日志。
- 非当前审批人不能处理待办。

## 22. 验收追踪矩阵

| 验收项 | 对应方案章节 |
| --- | --- |
| 员工可以创建并提交 5 类审批 | 6, 10, 12, 13 |
| 员工可以保存草稿 | 8, 10, 12 |
| 审批按默认流程流转 | 8, 9, 13 |
| 管理员可以配置流程 | 5.6, 6.2, 12.4 |
| 审批人可以处理待办 | 6.2, 12.3, 13 |
| 审批动作完整 | 8, 13 |
| 申请人查看进度和历史 | 6.2, 7.6, 16, 17 |
| 申请人撤回审批中申请 | 8, 13.8 |
| 驳回或补充材料后重新提交 | 8, 13.3, 13.4 |
| 管理员作废已通过申请 | 8, 13.9 |
| 角色数据权限正确 | 11, 18 |
| 系统内通知覆盖关键事件 | 15 |
| 24 小时超时提醒 | 15 |
| 审计日志追溯关键操作 | 16 |
| 工作台展示待办和动态 | 6.2, 12.8 |
| 审批列表支持筛选 | 14 |
| 报销和采购支持金额筛选 | 14 |
| 管理员和总经理可以导出 | 14 |

## 23. 第一版不做的技术范围

覆盖需求：REQ-014

第一版不实现：

- 移动端 App。
- 短信通知。
- 邮件通知。
- 企业微信集成。
- 钉钉集成。
- BPMN 流程设计器。
- 电子签章。
- 发票 OCR。
- 财务付款。
- 考勤系统。
- 薪资系统。
- 多公司或集团架构。
- 多语言。
- 合同审批。
- 用章审批。

## 24. 后续可拆分任务清单

### 后端任务

- 初始化 Spring Boot 项目。
- 配置 Maven 或 Gradle 构建。
- 配置 PostgreSQL 连接。
- 配置数据库迁移工具，优先使用 Flyway。
- 建立用户、组织、角色、权限表。
- 实现登录和当前用户接口。
- 实现 Spring Security 配置、JWT 过滤器和当前用户上下文。
- 实现 RBAC 权限注解或权限校验组件。
- 实现组织架构 API。
- 实现用户管理 API。
- 实现角色权限 API。
- 实现审批申请模型和 API。
- 实现附件上传。
- 实现流程模板模型和 API。
- 实现流程实例创建。
- 实现审批动作服务。
- 实现通知服务。
- 实现超时扫描任务。
- 实现审计日志服务。
- 实现工作台和统计 API。
- 实现导出 API。

### 前端任务

- 初始化 React 项目。
- 建立登录页和主布局。
- 建立路由和权限菜单。
- 实现工作台。
- 实现发起申请页面。
- 实现 5 类审批动态表单。
- 实现我的申请列表。
- 实现我的待办列表。
- 实现我的已办列表。
- 实现抄送我的列表。
- 实现审批详情页和时间线。
- 实现审批动作弹窗。
- 实现审批管理列表。
- 实现组织架构页面。
- 实现用户管理页面。
- 实现角色权限页面。
- 实现流程配置页面。
- 实现通知中心。
- 实现审计日志页面。

### 测试任务

- 编写状态机单元测试。
- 编写表单校验单元测试。
- 编写数据权限单元测试。
- 编写审批流程集成测试。
- 编写通知和审计集成测试。
- 编写核心端到端测试。
