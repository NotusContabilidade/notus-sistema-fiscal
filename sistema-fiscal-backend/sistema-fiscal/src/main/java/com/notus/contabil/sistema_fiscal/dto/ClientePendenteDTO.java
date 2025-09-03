package com.notus.contabil.sistema_fiscal.dto;

// DTO para a lista de clientes com cálculo pendente
public record ClientePendenteDTO(
    Long id,
    String razaoSocial
) {}