-- Adiciona a coluna cliente_id na tabela tasks
ALTER TABLE tasks ADD COLUMN cliente_id BIGINT;

-- Cria a FK para clientes
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_cliente
    FOREIGN KEY (cliente_id) REFERENCES clientes(id);