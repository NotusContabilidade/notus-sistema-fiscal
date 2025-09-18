package com.notus.contabil.sistema_fiscal.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notus.contabil.sistema_fiscal.entity.Calculo;
import com.notus.contabil.sistema_fiscal.entity.Cliente;
import com.notus.contabil.sistema_fiscal.entity.ParametrosSN;
import com.notus.contabil.sistema_fiscal.entity.ResultadoCalculoDetalhado;
import com.notus.contabil.sistema_fiscal.repository.CalculoRepository;
import com.notus.contabil.sistema_fiscal.repository.ClienteRepository;
import com.notus.contabil.sistema_fiscal.repository.ParametrosSNRepository;
import com.notus.contabil.sistema_fiscal.services.CalculadoraSimplesNacional;

@RestController
@RequestMapping("/api/calculos")
public class CalculoController {

    @Autowired
    private CalculoRepository calculoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ParametrosSNRepository parametrosSNRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // DTO para o histórico
    public record HistoricoCalculoDTO(Long id, int mesReferencia, int anoReferencia, double dasTotal) {}

    // DTO para o relatório detalhado
    public record CalculoCompletoDTO(Long id, int mesReferencia, int anoReferencia, double dasTotal, String dataCalculoFormatada, List<ResultadoCalculoDetalhado> detalhes) {}

    @Transactional(readOnly = true)
    @GetMapping("/historico/{clienteId}")
    public ResponseEntity<List<HistoricoCalculoDTO>> getHistoricoPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(
            calculoRepository.findAllByClienteIdOrderByAnoReferenciaDescMesReferenciaDesc(clienteId)
                .stream()
                .map(c -> new HistoricoCalculoDTO(c.getId(), c.getMesReferencia(), c.getAnoReferencia(), c.getDasTotal()))
                .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    @GetMapping("/{calculoId}")
    public ResponseEntity<?> getCalculoById(@PathVariable Long calculoId) {
        Optional<Calculo> calculoOpt = calculoRepository.findById(calculoId);
        
        if (calculoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Calculo calculo = calculoOpt.get();
        try {
            CalculoCompletoDTO dto = new CalculoCompletoDTO(
                calculo.getId(),
                calculo.getMesReferencia(),
                calculo.getAnoReferencia(),
                calculo.getDasTotal(),
                calculo.getDataCalculoFormatada(),
                calculo.getDetalhes()
            );
            return ResponseEntity.ok(dto);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Falha ao processar detalhes do cálculo."));
        }
    }

    public record CalculoRequestDTO(Long clienteId, int mesRef, int anoRef, Map<String, Map<String, Double>> receitas) {}

    @Transactional
    @PostMapping
    public ResponseEntity<?> executarCalculo(@RequestBody CalculoRequestDTO request) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(request.clienteId());
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", "Cliente não encontrado."));
        }

        Optional<ParametrosSN> paramsOpt = parametrosSNRepository.findByClienteId(request.clienteId());
        if (paramsOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", "Parâmetros fiscais não encontrados."));
        }
        
        ParametrosSN params = paramsOpt.get();

        // <-- INÍCIO DA CORREÇÃO: Validação da regra de negócio
        final double LIMITE_SIMPLES_NACIONAL = 4_800_000.00;
        if (params.getRbt12Atual() > LIMITE_SIMPLES_NACIONAL) {
            String mensagemErro = String.format(
                "O RBT12 do cliente (R$ %.2f) excede o teto do Simples Nacional (R$ %.2f). O cálculo não pode ser realizado.",
                params.getRbt12Atual(),
                LIMITE_SIMPLES_NACIONAL
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", mensagemErro));
        }
        // <-- FIM DA CORREÇÃO
        
        CalculadoraSimplesNacional.ResultadoGeralCalculo resultadoGeral = 
            CalculadoraSimplesNacional.calcularAtividadesConcomitantes(params.getRbt12Atual(), params.getFolhaPagamento12mAtual(), request.receitas());

        try {
            String detalhesJson = objectMapper.writeValueAsString(resultadoGeral.detalhes());
            
            Optional<Calculo> calculoExistenteOpt = calculoRepository.findByClienteIdAndMesReferenciaAndAnoReferencia(request.clienteId(), request.mesRef(), request.anoRef());
            Calculo calculoParaSalvar = calculoExistenteOpt.orElse(new Calculo());
            
            calculoParaSalvar.setCliente(clienteOpt.get());
            calculoParaSalvar.setMesReferencia(request.mesRef());
            calculoParaSalvar.setAnoReferencia(request.anoRef());
            calculoParaSalvar.setDasTotal(resultadoGeral.dasTotalGeral());
            calculoParaSalvar.setDetalhesJson(detalhesJson);

            Calculo calculoSalvo = calculoRepository.save(calculoParaSalvar);
            return ResponseEntity.ok(calculoSalvo);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erro", "Falha ao serializar detalhes."));
        }
    }
}