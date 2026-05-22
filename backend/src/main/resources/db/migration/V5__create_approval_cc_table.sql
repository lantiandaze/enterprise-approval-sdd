CREATE TABLE IF NOT EXISTS approval_cc (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    comment VARCHAR(1000),
    read_at TIMESTAMP WITH TIME ZONE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_approval_cc_request FOREIGN KEY (request_id) REFERENCES approval_request (id),
    CONSTRAINT fk_approval_cc_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
);

CREATE INDEX IF NOT EXISTS ix_approval_cc_user
    ON approval_cc (user_id, created_at);

CREATE INDEX IF NOT EXISTS ix_approval_cc_request
    ON approval_cc (request_id);
