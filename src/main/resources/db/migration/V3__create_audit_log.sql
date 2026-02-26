CREATE TABLE kyc_audit_log(
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    reason TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_request FOREIGN KEY(request_id) REFERENCES kyc_requests(id)
)