package com.notus.contabil.sistema_fiscal;
import java.util.Arrays;
import java.util.List;
public class AnexoIV implements TabelaSimplesNacional {
    private final List<Faixa> faixas = Arrays.asList(
        new Faixa(0.00, 180000.00, 0.045, 0.00, 18.80, 15.20, 23.95, 5.19, 0.00, 0.00, 36.86),
        new Faixa(180000.01, 360000.00, 0.090, 8100.00, 19.80, 16.20, 24.95, 5.41, 0.00, 0.00, 33.64),
        new Faixa(360000.01, 720000.00, 0.102, 12420.00, 20.80, 17.20, 25.95, 5.63, 0.00, 0.00, 30.42),
        new Faixa(720000.01, 1800000.00, 0.140, 39780.00, 17.80, 14.20, 26.95, 5.85, 0.00, 0.00, 35.20),
        new Faixa(1800000.01, 3600000.00, 0.220, 183780.00, 18.80, 15.20, 28.45, 6.17, 0.00, 0.00, 31.38),
        new Faixa(3600000.01, 4800000.00, 0.330, 828000.00, 53.50, 0.00, 21.50, 4.66, 20.34, 0.00, 0.00)
    );
    @Override public Faixa getFaixa(double rbt12) { return faixas.stream().filter(f -> rbt12 >= f.rbt12Min && rbt12 <= f.rbt12Max).findFirst().orElseThrow(() -> new IllegalArgumentException("RBT12 fora das faixas do Anexo IV")); }
}