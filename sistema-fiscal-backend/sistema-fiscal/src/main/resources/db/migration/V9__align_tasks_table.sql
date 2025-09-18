-- Remove colunas antigas e não utilizadas para manter o schema limpo.
-- A cláusula "IF EXISTS" garante que o script não falhe se as colunas já tiverem sido removidas.
ALTER TABLE tasks DROP COLUMN IF EXISTS responsavel_id;
ALTER TABLE tasks DROP COLUMN IF EXISTS criado_em;
ALTER TABLE tasks DROP COLUMN IF EXISTS atualizado_em;

-- Adiciona as colunas que estão na entidade Task.java mas não no banco de dados.
-- A cláusula "IF NOT EXISTS" garante que o script não falhe se as colunas já existirem.
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS responsavel VARCHAR(255);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS data_criacao TIMESTAMP;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS data_conclusao TIMESTAMP;