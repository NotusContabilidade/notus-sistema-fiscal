package com.notus.contabil.sistema_fiscal;

// DTO para a lista de clientes que tiveram DAS calculado no mês
public record ClienteFinanceiroDTO(
    Long id,
    String razaoSocial,
    Double dasTotal
) {}