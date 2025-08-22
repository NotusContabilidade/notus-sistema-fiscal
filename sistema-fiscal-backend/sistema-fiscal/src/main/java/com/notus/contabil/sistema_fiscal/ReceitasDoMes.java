package com.notus.contabil.sistema_fiscal;

/**
 * Objeto de dados para transportar as receitas segregadas do mês.
 */
public record ReceitasDoMes(double comTributacaoNormal, double comRetencaoISS, double comStICMS) {}