package com.notus.contabil.sistema_fiscal.dto;

public record DashboardStatsDTO(
    long totalClientes,
    Double totalDasNoMes,
    long clientesPendentes
) {}