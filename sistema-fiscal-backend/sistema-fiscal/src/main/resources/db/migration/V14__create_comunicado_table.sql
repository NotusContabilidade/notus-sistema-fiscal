CREATE TABLE comunicado (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    mensagem TEXT NOT NULL,
    data_criacao TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    cliente_id BIGINT,
    CONSTRAINT fk_comunicado_to_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

COMMENT ON COLUMN comunicado.cliente_id IS 'Pode ser nulo para indicar um comunicado em massa para todos os clientes.';