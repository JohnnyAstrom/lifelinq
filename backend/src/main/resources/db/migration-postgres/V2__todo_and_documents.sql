CREATE TABLE todos (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    text VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    scope VARCHAR(32) NOT NULL,
    due_date DATE NULL,
    due_time TIME NULL,
    scope_year INTEGER NULL,
    scope_week INTEGER NULL,
    scope_month INTEGER NULL,
    completed_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT fk_todos_group
        FOREIGN KEY (group_id)
        REFERENCES groups(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_todos_group
    ON todos(group_id);

CREATE INDEX idx_todos_group_scope
    ON todos(group_id, scope);

CREATE INDEX idx_todos_group_duedate
    ON todos(group_id, due_date);

CREATE INDEX idx_todos_deletedat
    ON todos(deleted_at);

CREATE TABLE documents (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    created_by_user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    notes VARCHAR(255) NULL,
    date DATE NULL,
    category VARCHAR(255) NULL,
    external_link VARCHAR(255) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_documents_group
        FOREIGN KEY (group_id)
        REFERENCES groups(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_documents_group_id
    ON documents(group_id);

CREATE TABLE document_tags (
    document_id UUID NOT NULL,
    tag VARCHAR(255) NULL,
    CONSTRAINT fk_document_tags_document
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_document_tags_document_id
    ON document_tags(document_id);
