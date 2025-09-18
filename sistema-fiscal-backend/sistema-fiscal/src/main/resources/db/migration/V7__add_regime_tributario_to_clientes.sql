-- Adiciona a coluna regime_tributario na tabela clientes

ALTER TABLE clientes ADD COLUMN IF NOT EXISTS regime_tributario VARCHAR(50) NOT NULL DEFAULT 'SIMPLES_NACIONAL';