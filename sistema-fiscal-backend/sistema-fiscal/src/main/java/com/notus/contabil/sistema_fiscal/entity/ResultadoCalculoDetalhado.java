package com.notus.contabil.sistema_fiscal.entity;

/**
 * Objeto de dados para transportar o resultado completo e detalhado do cálculo.
 */
public record ResultadoCalculoDetalhado(
    double rbt12, 
    double aliquotaEfetivaTotal, 
    Double fatorR, 
    String anexoAplicado,
    double rpaTotal, 
    double dasTotal,
    double rpaNormal, 
    double dasNormal,
    double rpaComRetencao, 
    double dasComRetencaoLiquido, 
    double issRetido,
    double rpaStICMS, 
    double dasStICMS
) {}