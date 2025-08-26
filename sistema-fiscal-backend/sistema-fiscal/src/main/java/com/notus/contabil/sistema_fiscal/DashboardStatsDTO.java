package com.notus.contabil.sistema_fiscal;

public record DashboardStatsDTO(
    long totalClientes,
    Double totalDasNoMes,
    long clientesPendentes
) {}