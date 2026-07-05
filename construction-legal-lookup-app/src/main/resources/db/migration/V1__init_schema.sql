CREATE TABLE users (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255)    NOT NULL,
    password_hash VARCHAR(255)    NOT NULL,
    full_name     VARCHAR(150)    NULL,
    role          VARCHAR(20)     NOT NULL DEFAULT 'USER',
    enabled       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at    DATETIME(6)     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_role (role),
    KEY idx_users_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE documents (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    document_number             VARCHAR(100)    NOT NULL,
    document_number_normalized  VARCHAR(100)    NOT NULL,
    title                       VARCHAR(500)    NOT NULL,
    abstract                    TEXT            NULL,
    document_type               VARCHAR(30)     NOT NULL,
    issuing_body                VARCHAR(255)    NULL,
    signer                      VARCHAR(255)    NULL,
    issued_date                 DATE            NULL,
    effective_date              DATE            NULL,
    expiry_date                 DATE            NULL,
    status                      VARCHAR(30)     NOT NULL DEFAULT 'CON_HIEU_LUC',
    field                       VARCHAR(100)    NULL DEFAULT 'XAY_DUNG',
    pdf_url                     VARCHAR(500)    NULL,
    pdf_file_name               VARCHAR(255)    NULL,
    pdf_size_bytes              BIGINT          NULL,
    content_text                LONGTEXT        NULL,
    content_html                LONGTEXT        NULL,
    search_text                 LONGTEXT        NULL,
    source_url                  VARCHAR(500)    NULL,
    view_count                  INT             NOT NULL DEFAULT 0,
    download_count              INT             NOT NULL DEFAULT 0,
    created_at                  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at                  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at                  DATETIME(6)     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_documents_number_normalized (document_number_normalized),
    KEY idx_documents_type (document_type),
    KEY idx_documents_status (status),
    KEY idx_documents_issuing_body (issuing_body),
    KEY idx_documents_issued_date (issued_date),
    KEY idx_documents_view_count (view_count),
    KEY idx_documents_updated_at (updated_at),
    FULLTEXT KEY ft_documents_search (title, abstract, content_text)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categories (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(150)    NOT NULL,
    slug          VARCHAR(150)    NOT NULL,
    description   TEXT            NULL,
    display_order INT             NOT NULL DEFAULT 0,
    created_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE document_categories (
    document_id   BIGINT          NOT NULL,
    category_id   BIGINT          NOT NULL,
    PRIMARY KEY (document_id, category_id),
    CONSTRAINT fk_doc_cat_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT fk_doc_cat_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tags (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100)    NOT NULL,
    slug          VARCHAR(100)    NOT NULL,
    created_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_tags_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE document_tags (
    document_id   BIGINT          NOT NULL,
    tag_id        BIGINT          NOT NULL,
    PRIMARY KEY (document_id, tag_id),
    CONSTRAINT fk_doc_tag_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT fk_doc_tag_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bookmarks (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    user_id       BIGINT          NOT NULL,
    document_id   BIGINT          NOT NULL,
    created_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_bookmarks_user_document (user_id, document_id),
    KEY idx_bookmarks_user_created (user_id, created_at DESC),
    CONSTRAINT fk_bookmarks_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmarks_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE search_history (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    user_id       BIGINT          NOT NULL,
    query         VARCHAR(500)    NULL,
    filters_json  JSON            NULL,
    result_count  INT             NULL,
    created_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_search_history_user_created (user_id, created_at DESC),
    CONSTRAINT fk_search_history_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE view_history (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    user_id       BIGINT          NOT NULL,
    document_id   BIGINT          NOT NULL,
    created_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_view_history_user_created (user_id, created_at DESC),
    KEY idx_view_history_document (document_id),
    CONSTRAINT fk_view_history_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_view_history_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    event_type    VARCHAR(30)     NOT NULL,
    user_id       BIGINT          NULL,
    document_id   BIGINT          NULL,
    ip_address    VARCHAR(45)     NULL,
    user_agent    VARCHAR(500)    NULL,
    metadata_json JSON            NULL,
    created_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_audit_event_created (event_type, created_at DESC),
    KEY idx_audit_user_created (user_id, created_at DESC),
    KEY idx_audit_document (document_id),
    KEY idx_audit_created (created_at DESC),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_document FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE crawl_logs (
    id                    BIGINT          NOT NULL AUTO_INCREMENT,
    status                VARCHAR(20)     NOT NULL,
    triggered_by          VARCHAR(50)     NULL,
    triggered_by_user_id  BIGINT          NULL,
    started_at            DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    finished_at           DATETIME(6)     NULL,
    inserted_count        INT             NOT NULL DEFAULT 0,
    updated_count         INT             NOT NULL DEFAULT 0,
    skipped_count         INT             NOT NULL DEFAULT 0,
    error_count           INT             NOT NULL DEFAULT 0,
    error_details         JSON            NULL,
    notes                 TEXT            NULL,
    PRIMARY KEY (id),
    KEY idx_crawl_logs_started (started_at DESC),
    CONSTRAINT fk_crawl_logs_user FOREIGN KEY (triggered_by_user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE invalidated_tokens (
    id          VARCHAR(255)    NOT NULL,
    expiry_time DATETIME(6)     NOT NULL,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

