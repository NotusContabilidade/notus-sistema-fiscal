-- Cria as tabelas para as listas de anexos e histórico, que são
-- gerenciadas pela anotação @ElementCollection na entidade Task.

CREATE TABLE IF NOT EXISTS task_anexos (
    task_id BIGINT NOT NULL,
    anexo VARCHAR(255),
    CONSTRAINT fk_task_anexos FOREIGN KEY (task_id) REFERENCES tasks(id)
);

CREATE TABLE IF NOT EXISTS task_historico (
    task_id BIGINT NOT NULL,
    historico TEXT,
    CONSTRAINT fk_task_historico FOREIGN KEY (task_id) REFERENCES tasks(id)
);