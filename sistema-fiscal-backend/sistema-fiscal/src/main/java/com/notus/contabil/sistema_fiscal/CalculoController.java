package com.notus.contabil.sistema_fiscal;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/calculos")
public class CalculoController {

    private final DatabaseManager dbManager = new DatabaseManager();

    // --- NOVO ENDPOINT PARA O HISTÓRICO ---
    @GetMapping("/historico/{clienteId}")
    public ResponseEntity<List<DatabaseManager.Calculo>> getHistoricoPorCliente(@PathVariable Long clienteId) {
        List<DatabaseManager.Calculo> historico = dbManager.calculoDAO.findAllByClienteId(clienteId);
        return ResponseEntity.ok(historico);
    }

    // O endpoint de executar o cálculo permanece o mesmo
    public record CalculoRequestDTO(
        Long clienteId, 
        int mesRef, 
        int anoRef, 
        Map<String, Map<String, Double>> receitas
    ) {}
    @PostMapping
    public ResponseEntity<?> executarCalculo(@RequestBody CalculoRequestDTO request) {
        // ... (código sem alterações)
        Optional<DatabaseManager.ParametrosSN> paramsOpt = dbManager.parametrosSNDAO.findByClienteId(request.clienteId());
        if (paramsOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(Map.of("erro", "Parâmetros fiscais não encontrados para o cliente ID: " + request.clienteId()));
        }
        DatabaseManager.ParametrosSN params = paramsOpt.get();
        CalculadoraSimplesNacional.ResultadoGeralCalculo resultadoGeral = 
            CalculadoraSimplesNacional.calcularAtividadesConcomitantes(
                params.rbt12Atual(),
                params.folhaPagamento12mAtual(),
                request.receitas()
            );
        long calculoId = dbManager.calculoDAO.salvar(request.clienteId(), request.mesRef(), request.anoRef(), resultadoGeral);
        Optional<DatabaseManager.Calculo> calculoSalvo = dbManager.calculoDAO.findById(calculoId);
        return ResponseEntity.ok(calculoSalvo.orElse(null));
    }
}
