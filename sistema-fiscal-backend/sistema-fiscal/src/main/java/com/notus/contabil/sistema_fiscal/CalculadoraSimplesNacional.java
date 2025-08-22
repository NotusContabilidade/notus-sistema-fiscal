package com.notus.contabil.sistema_fiscal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalculadoraSimplesNacional {

    // Novo record para encapsular o resultado geral
    public record ResultadoGeralCalculo(double dasTotalGeral, List<ResultadoCalculoDetalhado> detalhes) {}

    // O método de cálculo original permanece o mesmo, para ser usado internamente
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
        return new ResultadoCalculoDetalhado(rbt12, aliquotaEfetivaTotal, fatorRPercent, anexoAplicado, rpaTotal, dasTotal, receitas.comTributacaoNormal(), dasNormal, receitas.comRetencaoISS(), dasComRetencaoLiquido, issRetido, receitas.comStICMS(), dasStICMS);
    }

    // --- NOVO MÉTODO PARA ATIVIDADES CONCOMITANTES ---
    public static ResultadoGeralCalculo calcularAtividadesConcomitantes(double rbt12, Double folha12Meses, Map<String, Map<String, Double>> todasAsReceitas) {
        List<ResultadoCalculoDetalhado> detalhes = new ArrayList<>();
        double dasTotalGeral = 0;

        // Mapeia a chave do anexo para a sua instância
        Map<String, TabelaSimplesNacional> anexosMap = Map.of(
            "anexoI", new AnexoI(),
            "anexoII", new AnexoII(),
            "anexoIII", new AnexoIII(),
            "anexoIV", new AnexoIV(),
            "anexoV", new AnexoV()
        );

        // Itera sobre cada anexo que tem receita
        for (Map.Entry<String, Map<String, Double>> entry : todasAsReceitas.entrySet()) {
            String anexoKey = entry.getKey();
            Map<String, Double> valoresAnexo = entry.getValue();

            // Verifica se há alguma receita preenchida para este anexo
            if (valoresAnexo.values().stream().anyMatch(v -> v != null && v > 0)) {
                TabelaSimplesNacional anexo = anexosMap.get(anexoKey);
                ReceitasDoMes receitasDoMes = new ReceitasDoMes(
                    valoresAnexo.getOrDefault("rpaNormal", 0.0),
                    valoresAnexo.getOrDefault("rpaRetencao", 0.0),
                    valoresAnexo.getOrDefault("rpaSt", 0.0)
                );
                
                // O Fator R só é relevante se a atividade for do Anexo V
                Double folhaParaCalculo = (anexo instanceof AnexoV) ? folha12Meses : null;

                ResultadoCalculoDetalhado resultadoAnexo = calcular(anexo, rbt12, receitasDoMes, folhaParaCalculo);
                detalhes.add(resultadoAnexo);
                dasTotalGeral += resultadoAnexo.dasTotal();
            }
        }

        return new ResultadoGeralCalculo(dasTotalGeral, detalhes);
    }
}