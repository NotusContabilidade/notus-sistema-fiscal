-- Adiciona a nova coluna para guardar a chave do arquivo no storage.
-- A cláusula IF NOT EXISTS garante que o script não falhe se a coluna já existir por algum motivo.
ALTER TABLE documentos ADD COLUMN IF NOT EXISTS storage_key VARCHAR(1024);

-- Remove a coluna antiga que guardava o arquivo inteiro.
-- ATENÇÃO: Esta ação é destrutiva. Como estamos em desenvolvimento,
-- não há problema em remover a coluna. Em um ambiente de produção real,
-- seria necessário um passo intermediário para migrar os dados existentes.
ALTER TABLE documentos DROP COLUMN IF EXISTS conteudo;