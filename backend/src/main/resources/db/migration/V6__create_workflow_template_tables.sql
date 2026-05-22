CREATE TABLE IF NOT EXISTS approval_workflow_template (
    id BIGSERIAL PRIMARY KEY,
    approval_type VARCHAR(32) NOT NULL,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by BIGINT
);

CREATE INDEX IF NOT EXISTS ix_approval_workflow_template_type
    ON approval_workflow_template (approval_type, enabled, deleted);

CREATE TABLE IF NOT EXISTS approval_workflow_node_template (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    node_name VARCHAR(100) NOT NULL,
    approver_rule VARCHAR(64) NOT NULL,
    approver_user_id BIGINT,
    amount_greater_than NUMERIC(18, 2),
    sort_order INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_approval_workflow_node_template_template FOREIGN KEY (template_id) REFERENCES approval_workflow_template (id),
    CONSTRAINT fk_approval_workflow_node_template_user FOREIGN KEY (approver_user_id) REFERENCES sys_user (id)
);

CREATE INDEX IF NOT EXISTS ix_approval_workflow_node_template_template
    ON approval_workflow_node_template (template_id, sort_order);

CREATE TABLE IF NOT EXISTS approval_workflow_config_audit (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT,
    actor_id BIGINT NOT NULL,
    actor_name VARCHAR(100) NOT NULL,
    action VARCHAR(64) NOT NULL,
    detail VARCHAR(1000),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_approval_workflow_config_audit_template FOREIGN KEY (template_id) REFERENCES approval_workflow_template (id),
    CONSTRAINT fk_approval_workflow_config_audit_actor FOREIGN KEY (actor_id) REFERENCES sys_user (id)
);

CREATE INDEX IF NOT EXISTS ix_approval_workflow_config_audit_template
    ON approval_workflow_config_audit (template_id, created_at);

INSERT INTO approval_workflow_template (approval_type, name, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT 'leave', '请假默认流程', TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
WHERE NOT EXISTS (SELECT 1 FROM approval_workflow_template WHERE approval_type = 'leave' AND deleted = FALSE);

INSERT INTO approval_workflow_template (approval_type, name, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT 'overtime', '加班默认流程', TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
WHERE NOT EXISTS (SELECT 1 FROM approval_workflow_template WHERE approval_type = 'overtime' AND deleted = FALSE);

INSERT INTO approval_workflow_template (approval_type, name, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT 'business_trip', '出差默认流程', TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
WHERE NOT EXISTS (SELECT 1 FROM approval_workflow_template WHERE approval_type = 'business_trip' AND deleted = FALSE);

INSERT INTO approval_workflow_template (approval_type, name, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT 'expense', '报销默认流程', TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
WHERE NOT EXISTS (SELECT 1 FROM approval_workflow_template WHERE approval_type = 'expense' AND deleted = FALSE);

INSERT INTO approval_workflow_template (approval_type, name, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT 'purchase', '采购默认流程', TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
WHERE NOT EXISTS (SELECT 1 FROM approval_workflow_template WHERE approval_type = 'purchase' AND deleted = FALSE);

INSERT INTO approval_workflow_node_template (template_id, node_name, approver_rule, sort_order, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT t.id, '主管审批', 'direct_manager', 1, TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
FROM approval_workflow_template t
WHERE t.approval_type IN ('leave', 'overtime', 'business_trip') AND t.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM approval_workflow_node_template n WHERE n.template_id = t.id AND n.deleted = FALSE);

INSERT INTO approval_workflow_node_template (template_id, node_name, approver_rule, sort_order, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT t.id, '主管审批', 'direct_manager', 1, TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
FROM approval_workflow_template t
WHERE t.approval_type IN ('expense', 'purchase') AND t.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM approval_workflow_node_template n WHERE n.template_id = t.id AND n.sort_order = 1 AND n.deleted = FALSE);

INSERT INTO approval_workflow_node_template (template_id, node_name, approver_rule, sort_order, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT t.id, '财务审批', 'finance', 2, TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
FROM approval_workflow_template t
WHERE t.approval_type = 'expense' AND t.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM approval_workflow_node_template n WHERE n.template_id = t.id AND n.sort_order = 2 AND n.deleted = FALSE);

INSERT INTO approval_workflow_node_template (template_id, node_name, approver_rule, amount_greater_than, sort_order, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT t.id, '总经理审批', 'general_manager', 1000, 3, TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
FROM approval_workflow_template t
WHERE t.approval_type = 'expense' AND t.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM approval_workflow_node_template n WHERE n.template_id = t.id AND n.sort_order = 3 AND n.deleted = FALSE);

INSERT INTO approval_workflow_node_template (template_id, node_name, approver_rule, sort_order, enabled, deleted, created_at, created_by, updated_at, updated_by)
SELECT t.id, '总经理审批', 'general_manager', 2, TRUE, FALSE, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1
FROM approval_workflow_template t
WHERE t.approval_type = 'purchase' AND t.deleted = FALSE
  AND NOT EXISTS (SELECT 1 FROM approval_workflow_node_template n WHERE n.template_id = t.id AND n.sort_order = 2 AND n.deleted = FALSE);
