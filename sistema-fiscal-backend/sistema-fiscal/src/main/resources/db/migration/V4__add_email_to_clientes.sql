-- V4__add_email_to_clientes.sql
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS email VARCHAR(255) UNIQUE;