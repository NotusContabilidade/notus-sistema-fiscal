package com.notus.contabil.sistema_fiscal.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface TabelaSimplesNacional {
    class Faixa {
        public final double rbt12Min, rbt12Max, aliquotaNominal, parcelaADeduzir;
        public final double percentualIRPJ, percentualCSLL, percentualCOFINS, percentualPIS_PASEP;
        public final double percentualCPP, percentualICMS, percentualISS;

        public Faixa(double rbt12Min, double rbt12Max, double aliquotaNominal, double parcelaADeduzir,
                     double percentualIRPJ, double percentualCSLL, double percentualCOFINS,
                     double percentualPIS_PASEP, double percentualCPP, double percentualICMS,
                     double percentualISS) {
            this.rbt12Min = rbt12Min;
            this.rbt12Max = rbt12Max;
            this.aliquotaNominal = aliquotaNominal;
            this.parcelaADeduzir = parcelaADeduzir;
            this.percentualIRPJ = percentualIRPJ;
            this.percentualCSLL = percentualCSLL;
            this.percentualCOFINS = percentualCOFINS;
            this.percentualPIS_PASEP = percentualPIS_PASEP;
            this.percentualCPP = percentualCPP;
            this.percentualICMS = percentualICMS;
            this.percentualISS = percentualISS;
        }
    }

    Faixa getFaixa(double rbt12);

    default double calcularAliquotaEfetiva(double rbt12) {
        if (rbt12 <= 0) return 0;
        Faixa faixa = getFaixa(rbt12);
        BigDecimal bdRbt12 = BigDecimal.valueOf(rbt12);
        BigDecimal aliqNominal = BigDecimal.valueOf(faixa.aliquotaNominal);
        BigDecimal pd = BigDecimal.valueOf(faixa.parcelaADeduzir);

        BigDecimal aliquotaEfetiva = bdRbt12.multiply(aliqNominal).subtract(pd)
                .divide(bdRbt12, 10, RoundingMode.HALF_UP);
        
        return Math.max(0, aliquotaEfetiva.doubleValue());
    }
}