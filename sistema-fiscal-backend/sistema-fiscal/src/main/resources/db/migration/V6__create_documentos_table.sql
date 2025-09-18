-- Cria a tabela de documentos para cada tenant

CREATE TABLE IF NOT EXISTS documentos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    nome_arquivo VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    comentario TEXT,
    conteudo BYTEA,
    data_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_aprovacao TIMESTAMP,
    usuario_upload VARCHAR(255),
    usuario_aprovador VARCHAR(255)
);