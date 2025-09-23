-- Cria a tabela para armazenar os comentários das tarefas
CREATE TABLE IF NOT EXISTS task_comentarios (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    autor VARCHAR(255) NOT NULL,
    texto TEXT NOT NULL,
    data_criacao TIMESTAMP NOT NULL
);