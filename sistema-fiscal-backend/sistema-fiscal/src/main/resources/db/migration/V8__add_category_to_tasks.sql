-- Adiciona a coluna de categoria na tabela de tarefas
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS categoria VARCHAR(100);