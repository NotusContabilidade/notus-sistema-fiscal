package com.notus.contabil.sistema_fiscal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/vencimentos")
public class VencimentoController {

    @Autowired
    private VencimentoRepository vencimentoRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<VencimentoDTO>> getVencimentosPorPeriodo(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            // ✅ CORREÇÃO: Adicionamos o novo parâmetro de filtro.
            // `required = false` significa que a API funcionará mesmo se o filtro não for enviado.
            @RequestParam(required = false) String filtro) {
        
        // Se o filtro vier como uma string vazia do frontend, tratamos como nulo para a query.
        String filtroQuery = (filtro != null && !filtro.trim().isEmpty()) ? filtro.trim() : null;

        // ✅ CORREÇÃO: Chamamos o novo método do repositório, passando o filtro.
        List<Vencimento> vencimentos = vencimentoRepository.findVencimentosComFiltro(start, end, filtroQuery);

        List<VencimentoDTO> dtos = vencimentos.stream().map(v -> new VencimentoDTO(
                v.getId(),
                v.getDescricao() + " - " + v.getCliente().getRazaoSocial(),
                v.getDataVencimento().toString(),
                v.getDataVencimento().toString(),
                v.getStatus().name(),
                v.getCliente().getId(),
                v.getCliente().getRazaoSocial()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/cliente/{clienteId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<VencimentoDTO>> getVencimentosPorCliente(@PathVariable Long clienteId) {
        List<Vencimento> vencimentos = vencimentoRepository.findAllByClienteIdOrderByDataVencimentoDesc(clienteId);

        List<VencimentoDTO> dtos = vencimentos.stream().map(v -> new VencimentoDTO(
                v.getId(),
                v.getDescricao(),
                v.getDataVencimento().toString(),
                v.getDataVencimento().toString(),
                v.getStatus().name(),
                v.getCliente().getId(),
                v.getCliente().getRazaoSocial()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}