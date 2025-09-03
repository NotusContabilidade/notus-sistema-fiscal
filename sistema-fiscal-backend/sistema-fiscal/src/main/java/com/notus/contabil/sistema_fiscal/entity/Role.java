package com.notus.contabil.sistema_fiscal.entity;

/**
 * Enum que representa as funções (níveis de permissão) de um usuário no sistema.
 * Usar um Enum garante que apenas valores válidos possam ser atribuídos a um usuário,
 * aumentando a segurança e a manutenibilidade do código.
 */
public enum Role {
    /**
     * Usuário padrão, com permissões para as operações do dia a dia.
     */
    USER,

    /**
     * Administrador, com permissões elevadas para gerenciar o sistema ou outros usuários.
     */
    ADMIN
}