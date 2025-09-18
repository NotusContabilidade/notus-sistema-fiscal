-- Versão corrigida da tabela de tarefas, alinhada com a entidade Task.java
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255),
    descricao TEXT,
    status VARCHAR(50),
    prazo DATE,
    responsavel VARCHAR(255),
    categoria VARCHAR(100),
    data_criacao TIMESTAMP,
    data_conclusao TIMESTAMP,
    cliente_id BIGINT REFERENCES clientes(id)
);

-- Tabelas para as listas de anexos e histórico
CREATE TABLE IF NOT EXISTS task_anexos (
    task_id BIGINT NOT NULL REFERENCES tasks(id),
    anexo VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS task_historico (
    task_id BIGINT NOT NULL REFERENCES tasks(id),
    historico TEXT
);