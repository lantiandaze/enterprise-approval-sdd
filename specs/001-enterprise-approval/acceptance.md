# 企业审批系统验收追踪记录

本文档用于阶段八验收和收口，追踪 `spec.md`、`plan.md` 和 `tasks.md` 中第一版能力的完成证据。

## 验收结论

第一版企业审批系统已经形成可运行闭环：用户可以登录、提交审批申请、进入默认或配置流程、审批人处理待办、申请人查看进度，管理员可以管理组织用户、配置流程、查询历史、查看审计、筛选导出和查看统计。

当前验收状态：通过，带少量后续增强项。

## 验收矩阵

| 验收项 | 覆盖范围 | 证据 | 结论 |
| --- | --- | --- | --- |
| 登录和当前用户 | 登录、JWT、当前用户、角色权限返回 | 已实现 `/api/auth/login`、`/api/auth/me`，前端登录后进入主布局 | 通过 |
| 组织和账号管理 | 部门、岗位、用户、角色、权限 | 已实现组织管理、用户管理、角色权限页面和 API | 通过 |
| 数据权限 | 普通员工、主管、财务、人事、总经理、管理员范围控制 | `DataPermissionServiceTest` 覆盖管理员、主管、员工关键规则 | 通过 |
| 审批申请 | 请假、报销、采购、加班、出差 | 已实现动态表单、草稿、提交、我的申请和详情 | 通过 |
| 表单校验 | 必填、金额、时间顺序、附件规则 | `ApprovalFormValidatorTest` 覆盖有效请假、非法时间、非法金额 | 通过 |
| 附件 | 上传、下载、删除、权限校验 | 已实现附件 API 和详情页入口，删除采用软删除 | 通过 |
| 审批闭环 | 提交、待办、同意、最终通过 | `ApprovalWorkflowIntegrationTest` 使用 PostgreSQL 验证员工提交、主管待办、同意、通知 | 通过 |
| 高级审批动作 | 驳回、撤回、补充材料、转交、加签、抄送、作废 | 阶段四已通过真实接口验证，并写入动作记录 | 通过 |
| 流程配置 | 模板、节点、审批人规则、启停、审计 | 已实现流程配置 API、页面和配置审计 | 通过 |
| 通知 | 待办、审批结果、补充、转交、加签、抄送、超时 | 已实现通知模型、通知中心、未读数和标记已读 | 通过 |
| 超时 | 24 小时待办提醒 | 已实现定时扫描、超时标记和去重通知 | 通过 |
| 审计 | 审批动作、流程配置、导出记录 | 已实现审计 API 和审计日志页面 | 通过 |
| 管理查询 | 全局列表、权限范围、类型/状态/申请人/部门/时间/金额筛选 | 阶段七已通过接口验证管理、员工范围和筛选 | 通过 |
| 导出 | CSV 导出、筛选复用、权限控制 | 阶段七已验证管理员导出成功、普通用户被拒绝 | 通过 |
| 工作台和统计 | 待办、进行中、未读通知、全局汇总、类型/状态分布 | 阶段七已通过接口验证 | 通过 |
| 后端自动化测试 | 单元测试和集成测试 | `mvn test` 通过，7 个测试全部成功 | 通过 |
| 前端构建验证 | TypeScript 类型检查和生产构建 | `npm.cmd run build` 通过 | 通过 |
| 本地运行说明 | 数据库、后端、前端、账号、测试命令 | 已更新 `README.md` | 通过 |

## 已执行命令

```powershell
.\tools\apache-maven-3.9.9\bin\mvn.cmd -f .\backend\pom.xml test
```

结果：

```text
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

```powershell
cd .\frontend
npm.cmd run build
```

结果：

```text
tsc && vite build
✓ built
```

## 后续增强项

- 前端目前以 `tsc && vite build` 作为正式校验，尚未引入 Playwright 或组件测试体系。建议后续单独建设端到端测试，覆盖登录、提交、审批、筛选和导出。
- 24 小时超时扫描逻辑已实现，但真实 24 小时等待场景未在本阶段长时间等待验证，后续可增加可控时间源或专门集成测试。

## 阶段八后续修缮（2026-05-25）

针对阶段八后通过黑盒测试发现的偏差，按八个步骤完成修复，详见 `tasks.md` 第 9 节：

1. `DataPermissionService` 给 `finance` / `hr` 增加 `allowedApprovalTypes`：分别限定为 `expense` 与 `leave/overtime`，`general_manager` / `admin` 保持全量；补充 3 个单测分支，后端 `mvn test` 由 7 个增至 10 个全部通过。
2. `ApprovalWorkflowService.resolveRules` 恢复 spec 6.2 默认流程：`leave` 增加「人事确认（hr）」节点、`business_trip` 增加「总经理审批」节点；新增 Flyway 迁移 `V8__align_default_workflow_templates_with_spec.sql` 对存量数据库幂等补节点；`InitialDataSeeder` 增加 `hr01` 用户与 HR 岗位。
3. `voidRequest` 仅允许 `approved -> voided`，作废原因不可为空，其它状态返回 `BAD_REQUEST` 而非 500。
4. 审批人解析移除「无可用审批人时回退到申请人」的回退路径，并显式拒绝申请人解析为自身审批人，避免自我审批越权。
5. `ApprovalManagementService` 列表改为 `Pageable` 分页并新增 `PagedResult<T>` 包装；统计接口改为多次 `count()` 聚合查询；导出按页拉取（硬上限 5000）。前端 `App.tsx` 同步使用 Ant Design `Table` 的分页器。
6. `JwtTokenService` 在 `@PostConstruct` 校验默认密钥：非开发环境若未覆盖默认密钥则启动失败，开发环境记录 WARN；`InitialDataSeeder` 补齐 plan 18.2 全量操作权限点（approval.*、organization.manage、user.manage、role.manage、workflow.manage、audit.view、dashboard.statistics.view），并按角色矩阵授权。
7. 前端 `AppShell` 改为按 `user.permissions` 过滤侧边栏与页面渲染；`vite.config.ts` 引入 `manualChunks` 把 antd / react / tanstack 拆为独立 chunk，主入口 JS 由约 1306 kB 降至约 46 kB。
8. `plan.md` 同步：12.3 API 路径表、11.1 数据权限矩阵、19.1 性能、19.2 安全（JWT 密钥要求）、5.4 角色码已更新。`tasks.md` 第 9 节追加阶段九修缮任务条目。

修缮后基线：
- 后端 `mvn test` 10/10 通过，`Tests run: 10, Failures: 0, Errors: 0, Skipped: 0`。
- 前端 `npm run build` 通过，主 chunk 拆分后默认 chunkSizeWarningLimit = 800 kB；antd 单独 chunk 约 985 kB（gzip 309 kB）。
- Flyway 已生效 V8 迁移。
- 端到端验证：员工提交请假后生成 主管→HR 两节点；admin 对 in_progress 作废返回干净 `BAD_REQUEST`；finance 管理列表仅返回 `expense`；hr 管理列表仅返回 `leave/overtime`；`/api/approval-management` 支持 `page`/`pageSize` 分页参数。
