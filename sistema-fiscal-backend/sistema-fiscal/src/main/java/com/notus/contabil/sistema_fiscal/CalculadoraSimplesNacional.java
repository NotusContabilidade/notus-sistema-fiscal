package com.notus.contabil.sistema_fiscal;

public class CalculadoraSimplesNacional {

    public static ResultadoCalculoDetalhado calcular(TabelaSimplesNacional anexo, double rbt12, ReceitasDoMes receitas, Double folha12Meses) {
        TabelaSimplesNacional anexoCalculo = anexo;
        Double fatorRPercent = null;
        String anexoAplicado = anexo.getClass().getSimpleName();

        if (folha12Meses != null) {
            double fatorR = (rbt12 > 0) ? folha12Meses / rbt12 : 0;
            fatorRPercent = fatorR * 100.0;
            anexoCalculo = (fatorR >= 0.28) ? new AnexoIII() : new AnexoV();
            anexoAplicado = anexoCalculo.getClass().getSimpleName();
        }

        TabelaSimplesNacional.Faixa faixa = anexoCalculo.getFaixa(rbt12);
        double aliquotaEfetivaTotal = anexoCalculo.calcularAliquotaEfetiva(rbt12);

        double aliquotaEfetivaICMS = aliquotaEfetivaTotal * (faixa.percentualICMS / 100.0);
        double aliquotaEfetivaISS = aliquotaEfetivaTotal * (faixa.percentualISS / 100.0);

        double dasNormal = receitas.comTributacaoNormal() * aliquotaEfetivaTotal;
        double dasStICMS = receitas.comStICMS() * Math.max(0, aliquotaEfetivaTotal - aliquotaEfetivaICMS);
        
        double issRetido = receitas.comRetencaoISS() * aliquotaEfetivaISS;
        double dasComRetencaoLiquido = receitas.comRetencaoISS() * Math.max(0, aliquotaEfetivaTotal - aliquotaEfetivaISS);

        double rpaTotal = receitas.comTributacaoNormal() + receitas.comRetencaoISS() + receitas.comStICMS();
        double dasTotal = dasNormal + dasStICMS + dasComRetencaoLiquido;

        return new ResultadoCalculoDetalhado(
            rbt12, aliquotaEfetivaTotal, fatorRPercent, anexoAplicado,
            rpaTotal, dasTotal,
            receitas.comTributacaoNormal(), dasNormal,
            receitas.comRetencaoISS(), dasComRetencaoLiquido, issRetido,
            receitas.comStICMS(), dasStICMS
        );
    }
}