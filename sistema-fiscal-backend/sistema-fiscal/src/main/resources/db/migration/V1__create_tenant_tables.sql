-- Script para criar a estrutura de tabelas para um novo tenant (escritório).

-- Tabela de Clientes do escritório
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    cnpj VARCHAR(14) NOT NULL UNIQUE,
    razao_social VARCHAR(255) NOT NULL
);

-- Tabela de Parâmetros Fiscais de cada cliente
CREATE TABLE parametros_sn (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL UNIQUE REFERENCES clientes(id),
    rbt12_atual DOUBLE PRECISION NOT NULL,
    folha_pagamento_12m_atual DOUBLE PRECISION NOT NULL
);

-- Tabela para armazenar os cálculos realizados
CREATE TABLE calculos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    mes_referencia INT NOT NULL,
    ano_referencia INT NOT NULL,
    das_total DOUBLE PRECISION NOT NULL,
    data_calculo TIMESTAMP,
    detalhes_json JSONB
);

-- Tabela para o controle de vencimentos
CREATE TABLE vencimentos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    descricao VARCHAR(255) NOT NULL,
    data_vencimento DATE NOT NULL,
    status VARCHAR(255) NOT NULL
);

-- Adicione aqui qualquer outra tabela que seja específica de um escritório...