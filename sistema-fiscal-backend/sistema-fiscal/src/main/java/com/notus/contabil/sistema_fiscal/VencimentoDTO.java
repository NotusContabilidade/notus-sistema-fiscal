package com.notus.contabil.sistema_fiscal;

public record VencimentoDTO(
    Long id,
    String title, // O título que aparecerá no evento do calendário
    String start, // A data de início no formato "YYYY-MM-DD"
    String end,   // A data de fim (será a mesma para eventos de um dia)
    String status,
    Long clienteId,
    String nomeCliente
) {}