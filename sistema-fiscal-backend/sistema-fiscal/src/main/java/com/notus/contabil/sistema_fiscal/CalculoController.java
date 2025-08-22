package com.notus.contabil.sistema_fiscal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;
import java.util.Optional;

// Adicionado para permitir requisições do frontend React
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/calculos")
public class CalculoController {

    private final DatabaseManager dbManager = new DatabaseManager();

    public record CalculoRequestDTO(
        Long clienteId, 
        int mesRef, 
        int anoRef, 
        Map<String, String> receitas
    ) {}

    @PostMapping
    public ResponseEntity<?> executarCalculo(@RequestBody CalculoRequestDTO request) {
        Optional<DatabaseManager.ParametrosSN> paramsOpt = dbManager.parametrosSNDAO.findByClienteId(request.clienteId());
        
        if (paramsOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(Map.of("erro", "Parâmetros fiscais não encontrados para o cliente ID: " + request.clienteId()));
        }
        
        DatabaseManager.ParametrosSN params = paramsOpt.get();
        TabelaSimplesNacional anexo;
        ReceitasDoMes receitasDoMes;
        Double folha12Meses = params.folhaPagamento12mAtual();

        if (isReceitaPreenchida(request.receitas(), "anexoI")) {
            anexo = new AnexoI();
            receitasDoMes = extrairReceitas(request.receitas(), "anexoI");
        } else if (isReceitaPreenchida(request.receitas(), "anexoII")) {
            anexo = new AnexoII();
            receitasDoMes = extrairReceitas(request.receitas(), "anexoII");
        } else if (isReceitaPreenchida(request.receitas(), "anexoIII")) {
            anexo = new AnexoIII();
            receitasDoMes = extrairReceitas(request.receitas(), "anexoIII");
        } else if (isReceitaPreenchida(request.receitas(), "anexoIV")) {
            anexo = new AnexoIV();
            receitasDoMes = extrairReceitas(request.receitas(), "anexoIV");
        } else if (isReceitaPreenchida(request.receitas(), "anexoV")) {
            anexo = new AnexoV();
            receitasDoMes = extrairReceitas(request.receitas(), "anexoV");
        } else {
            return ResponseEntity.badRequest().body(Map.of("erro", "Nenhuma receita foi informada."));
        }

        ResultadoCalculoDetalhado resultado = CalculadoraSimplesNacional.calcular(
            anexo,
            params.rbt12Atual(),
            receitasDoMes,
            (anexo instanceof AnexoV) ? folha12Meses : null
        );

        long calculoId = dbManager.calculoDAO.salvar(request.clienteId(), request.mesRef(), request.anoRef(), resultado);
        
        Optional<DatabaseManager.Calculo> calculoSalvo = dbManager.calculoDAO.findById(calculoId);

        return ResponseEntity.ok(calculoSalvo.orElse(null));
    }

    private boolean isReceitaPreenchida(Map<String, String> receitas, String prefixoAnexo) {
        return parseDouble(receitas.get(prefixoAnexo + "_rpaNormal")) > 0 ||
               parseDouble(receitas.get(prefixoAnexo + "_rpaSt")) > 0 ||
               parseDouble(receitas.get(prefixoAnexo + "_rpaRetencao")) > 0;
    }

    private ReceitasDoMes extrairReceitas(Map<String, String> receitas, String prefixoAnexo) {
        double rpaNormal = parseDouble(receitas.get(prefixoAnexo + "_rpaNormal"));
        double rpaSt = parseDouble(receitas.get(prefixoAnexo + "_rpaSt"));
        double rpaRetencao = parseDouble(receitas.get(prefixoAnexo + "_rpaRetencao"));
        return new ReceitasDoMes(rpaNormal, rpaRetencao, rpaSt);
    }

    private double parseDouble(String value) {
        if (value == null || value.isBlank()) return 0.0;
        try { return Double.parseDouble(value); } catch (NumberFormatException e) { return 0.0; }
    }
}
