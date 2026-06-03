-- Usuario local auto-provisionado desde el JWT.
CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Categorias propias del usuario.
CREATE TABLE IF NOT EXISTS task_categories (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_users (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(16),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_task_categories_owner ON task_categories (owner_id);

-- Tareas / planificacion.
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_users (id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    notes TEXT,
    status VARCHAR(20) NOT NULL,
    importance VARCHAR(20) NOT NULL,
    category_id BIGINT REFERENCES task_categories (id) ON DELETE SET NULL,
    scheduled_date DATE,
    start_time TIME,
    end_time TIME,
    color VARCHAR(16),
    day_order INTEGER NOT NULL DEFAULT 0,
    recurrence_freq VARCHAR(20) NOT NULL DEFAULT 'NONE',
    recurrence_interval INTEGER NOT NULL DEFAULT 1,
    recurrence_end_type VARCHAR(20) NOT NULL DEFAULT 'NEVER',
    recurrence_end_date DATE,
    recurrence_count INTEGER,
    completed_at TIMESTAMP,
    planned_notified BOOLEAN NOT NULL DEFAULT FALSE,
    completed_notified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tasks_owner_date ON tasks (owner_id, scheduled_date);
CREATE INDEX IF NOT EXISTS idx_tasks_owner_status ON tasks (owner_id, status);
CREATE INDEX IF NOT EXISTS idx_tasks_owner_importance ON tasks (owner_id, importance);
