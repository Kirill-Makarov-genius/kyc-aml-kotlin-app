ALTER TABLE kyc_requests ADD COLUMN risk_score INT DEFAULT 0 NOT NULL;
ALTER TABLE kyc_requests ADD COLUMN internal_comment VARCHAR(255);