-- Felicitaciones otorgadas (cada 5 completadas / 5 planificadas).
CREATE TABLE IF NOT EXISTS achievements (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_users (id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    milestone INTEGER NOT NULL,
    awarded_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_achievements_owner ON achievements (owner_id, awarded_at);

-- Historial de tareas completadas (sobrevive al borrado de la tarea).
CREATE TABLE IF NOT EXISTS task_completion_history (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_users (id) ON DELETE CASCADE,
    task_id BIGINT REFERENCES tasks (id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    importance VARCHAR(20) NOT NULL,
    completed_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_completion_history_owner ON task_completion_history (owner_id, completed_at);
