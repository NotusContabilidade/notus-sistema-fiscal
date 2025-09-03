-- Script para criar a estrutura de tabelas para um novo tenant (escritório).
CREATE SCHEMA IF NOT EXISTS "escritorio_A";

-- Tabela de Usuários (NOVA ADIÇÃO)
CREATE TABLE IF NOT EXISTS "escritorio_A".users (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50),
    tenant_id VARCHAR(50)
);

-- Tabela de Clientes do escritório
CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    cnpj VARCHAR(14) NOT NULL UNIQUE,
    razao_social VARCHAR(255) NOT NULL
);

-- Tabela de Parâmetros Fiscais de cada cliente
CREATE TABLE IF NOT EXISTS parametros_sn (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL UNIQUE REFERENCES clientes(id),
    rbt12_atual DOUBLE PRECISION NOT NULL,
    folha_pagamento_12m_atual DOUBLE PRECISION NOT NULL
);

-- Tabela para armazenar os cálculos realizados
CREATE TABLE IF NOT EXISTS calculos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    mes_referencia INT NOT NULL,
    ano_referencia INT NOT NULL,
    das_total DOUBLE PRECISION NOT NULL,
    data_calculo TIMESTAMP,
    detalhes_json JSONB
);

-- Tabela para o controle de vencimentos
CREATE TABLE IF NOT EXISTS vencimentos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    descricao VARCHAR(255) NOT NULL,
    data_vencimento DATE NOT NULL,
    status VARCHAR(255) NOT NULL
);

-- Adicione aqui qualquer outra tabela que seja específica de um escritório...