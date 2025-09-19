-- Cria a tabela para armazenar os moldes de tarefas recorrentes

CREATE TABLE IF NOT EXISTS tarefas_recorrentes (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    categoria VARCHAR(100),
    responsavel VARCHAR(255),
    frequencia VARCHAR(50) NOT NULL,
    dia_vencimento INT NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT true
);