package com.notus.contabil.sistema_fiscal.services;
import java.util.Arrays;
import java.util.List;

import com.notus.contabil.sistema_fiscal.entity.TabelaSimplesNacional;

public class AnexoII implements TabelaSimplesNacional {
    private final List<Faixa> faixas = Arrays.asList(
        new Faixa(0.00, 180000.00, 0.045, 0.00, 5.50, 3.50, 12.74, 2.76, 41.50, 34.00, 0.00),
        new Faixa(180000.01, 360000.00, 0.078, 5940.00, 5.50, 3.50, 12.74, 2.76, 41.50, 34.00, 0.00),
        new Faixa(360000.01, 720000.00, 0.100, 13860.00, 5.50, 3.50, 12.74, 2.76, 42.00, 33.50, 0.00),
        new Faixa(720000.01, 1800000.00, 0.112, 22500.00, 5.50, 3.50, 12.74, 2.76, 42.00, 33.50, 0.00),
        new Faixa(1800000.01, 3600000.00, 0.147, 85500.00, 5.50, 3.50, 12.74, 2.76, 42.00, 33.50, 0.00),
        new Faixa(3600000.01, 4800000.00, 0.300, 720000.00, 13.50, 10.00, 28.27, 6.13, 42.10, 0.00, 0.00)
    );
    @Override public Faixa getFaixa(double rbt12) { return faixas.stream().filter(f -> rbt12 >= f.rbt12Min && rbt12 <= f.rbt12Max).findFirst().orElseThrow(() -> new IllegalArgumentException("RBT12 fora das faixas do Anexo II")); }
}