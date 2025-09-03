package com.notus.contabil.sistema_fiscal.services;
import java.util.Arrays;
import java.util.List;

import com.notus.contabil.sistema_fiscal.entity.TabelaSimplesNacional;

public class AnexoIII implements TabelaSimplesNacional {
    private final List<Faixa> faixas = Arrays.asList(
        new Faixa(0.00, 180000.00, 0.060, 0.00, 4.00, 4.00, 12.82, 2.78, 43.40, 0.00, 33.50),
        new Faixa(180000.01, 360000.00, 0.112, 9360.00, 4.00, 4.00, 12.82, 2.78, 43.40, 0.00, 32.00),
        new Faixa(360000.01, 720000.00, 0.135, 17640.00, 3.50, 3.50, 14.05, 3.05, 43.40, 0.00, 32.50),
        new Faixa(720000.01, 1800000.00, 0.160, 35640.00, 3.50, 3.50, 13.64, 2.96, 43.90, 0.00, 32.50),
        new Faixa(1800000.01, 3600000.00, 0.210, 125640.00, 3.50, 3.50, 12.82, 2.78, 46.40, 0.00, 31.00),
        new Faixa(3600000.01, 4800000.00, 0.330, 648000.00, 30.00, 15.00, 19.28, 4.18, 31.54, 0.00, 0.00)
    );
    @Override public Faixa getFaixa(double rbt12) { return faixas.stream().filter(f -> rbt12 >= f.rbt12Min && rbt12 <= f.rbt12Max).findFirst().orElseThrow(() -> new IllegalArgumentException("RBT12 fora das faixas do Anexo III")); }
}