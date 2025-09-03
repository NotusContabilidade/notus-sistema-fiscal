package com.notus.contabil.sistema_fiscal.services;
import java.util.Arrays;
import java.util.List;

import com.notus.contabil.sistema_fiscal.entity.TabelaSimplesNacional;


public class AnexoV implements TabelaSimplesNacional {
    private final List<Faixa> faixas = Arrays.asList(
        new Faixa(0.00, 180000.00, 0.155, 0.00, 25.00, 15.00, 17.05, 3.70, 28.25, 0.00, 11.00),
        new Faixa(180000.01, 360000.00, 0.180, 4500.00, 23.00, 15.00, 17.05, 3.70, 27.25, 0.00, 14.00),
        new Faixa(360000.01, 720000.00, 0.195, 9900.00, 23.00, 15.00, 16.55, 3.60, 26.85, 0.00, 15.00),
        new Faixa(720000.01, 1800000.00, 0.205, 17100.00, 21.00, 12.50, 18.55, 4.03, 28.92, 0.00, 15.00),
        new Faixa(1800000.01, 3600000.00, 0.230, 62100.00, 23.00, 13.50, 17.55, 3.82, 26.13, 0.00, 16.00),
        new Faixa(3600000.01, 4800000.00, 0.305, 540000.00, 35.00, 15.50, 19.05, 4.14, 26.31, 0.00, 0.00)
    );
    @Override public Faixa getFaixa(double rbt12) { return faixas.stream().filter(f -> rbt12 >= f.rbt12Min && rbt12 <= f.rbt12Max).findFirst().orElseThrow(() -> new IllegalArgumentException("RBT12 fora das faixas do Anexo V")); }
}