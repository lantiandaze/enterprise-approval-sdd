-- V8: 补齐默认流程缺失节点
-- 来源：spec.md 第 6.2 章, plan.md 第 9 章
-- 该迁移按业务域使用 approval_ 前缀，仅 INSERT，不改动现有节点，符合 AGENTS.md 第 4 章软删除/非破坏性约束。

-- 请假默认流程需要在主管审批后追加 “人事确认” 节点（spec 6.2.1）
INSERT INTO approval_workflow_node_template
    (template_id, node_name, approver_rule, sort_order, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT t.id, '人事确认', 'hr', 2, TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
FROM approval_workflow_template t
WHERE t.approval_type = 'leave'
  AND t.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM approval_workflow_node_template n
      WHERE n.template_id = t.id
        AND n.deleted = FALSE
        AND n.approver_rule = 'hr'
  );

-- 出差默认流程需要在主管审批后追加 “总经理审批” 节点（spec 6.2.5）
INSERT INTO approval_workflow_node_template
    (template_id, node_name, approver_rule, sort_order, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT t.id, '总经理审批', 'general_manager', 2, TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
FROM approval_workflow_template t
WHERE t.approval_type = 'business_trip'
  AND t.deleted = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM approval_workflow_node_template n
      WHERE n.template_id = t.id
        AND n.deleted = FALSE
        AND n.approver_rule = 'general_manager'
  );
