CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(64) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(1000),
    related_request_id BIGINT,
    related_task_id BIGINT,
    read_at TIMESTAMP WITH TIME ZONE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_notification_request FOREIGN KEY (related_request_id) REFERENCES approval_request (id),
    CONSTRAINT fk_notification_task FOREIGN KEY (related_task_id) REFERENCES approval_task (id)
);

CREATE INDEX IF NOT EXISTS ix_notification_user_read
    ON notification (user_id, read_at, created_at);

CREATE INDEX IF NOT EXISTS ix_notification_related_task
    ON notification (related_task_id, type);
