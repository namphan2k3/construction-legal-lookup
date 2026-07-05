-- V4__increase_column_sizes.sql: Increase column sizes to fit longer data from VBPL dataset

ALTER TABLE documents 
DROP INDEX idx_documents_issuing_body,
MODIFY COLUMN document_number VARCHAR(255) NOT NULL,
MODIFY COLUMN document_number_normalized VARCHAR(255) NOT NULL,
MODIFY COLUMN title TEXT NOT NULL,
MODIFY COLUMN issuing_body TEXT NULL,
MODIFY COLUMN signer TEXT NULL,
MODIFY COLUMN source_url TEXT NULL;
