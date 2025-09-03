-- Script para criar tabelas globais, compartilhadas por todo o sistema.

CREATE TABLE IF NOT EXISTS offices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);