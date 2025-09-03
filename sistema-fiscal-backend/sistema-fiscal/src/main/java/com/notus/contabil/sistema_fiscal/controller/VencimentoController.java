package com.notus.contabil.sistema_fiscal.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.notus.contabil.sistema_fiscal.dto.VencimentoDTO;
import com.notus.contabil.sistema_fiscal.entity.Cliente;
import com.notus.contabil.sistema_fiscal.entity.Vencimento;
import com.notus.contabil.sistema_fiscal.repository.ClienteRepository;
import com.notus.contabil.sistema_fiscal.repository.VencimentoRepository;


@RestController
@RequestMapping("/api/vencimentos")
public class VencimentoController {

    @Autowired
    private VencimentoRepository vencimentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public record VencimentoRequestDTO(
        Long clienteId,
        String descricao,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate dataVencimento,
        Vencimento.StatusVencimento status
    ) {}

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<VencimentoDTO>> getVencimentosPorPeriodo(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String filtro) {
        
        String filtroQuery = null;
        if (filtro != null && !filtro.trim().isEmpty()) {
            String apenasNumeros = filtro.replaceAll("[^0-9]", "");
            if (apenasNumeros.length() > 0 && apenasNumeros.length() <= 14) {
                 filtroQuery = apenasNumeros;
            } else {
                filtroQuery = filtro.trim();
            }
        }

        List<Vencimento> vencimentos = vencimentoRepository.findVencimentosComFiltro(start, end, filtroQuery);
        List<VencimentoDTO> dtos = vencimentos.stream().map(v -> new VencimentoDTO(v.getId(), v.getDescricao() + " - " + v.getCliente().getRazaoSocial(), v.getDataVencimento().toString(), v.getDataVencimento().toString(), v.getStatus().name(), v.getCliente().getId(), v.getCliente().getRazaoSocial() )).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/cliente/{clienteId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<VencimentoDTO>> getVencimentosPorCliente(@PathVariable Long clienteId) {
        List<Vencimento> vencimentos = vencimentoRepository.findAllByClienteIdOrderByDataVencimentoDesc(clienteId);
        List<VencimentoDTO> dtos = vencimentos.stream().map(v -> new VencimentoDTO(v.getId(), v.getDescricao(), v.getDataVencimento().toString(), v.getDataVencimento().toString(), v.getStatus().name(), v.getCliente().getId(), v.getCliente().getRazaoSocial() )).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Vencimento> criarVencimento(@RequestBody VencimentoRequestDTO request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente não encontrado com o ID fornecido."));

        Vencimento novoVencimento = new Vencimento();
        novoVencimento.setCliente(cliente);
        novoVencimento.setDescricao(request.descricao());
        novoVencimento.setDataVencimento(request.dataVencimento());
        novoVencimento.setStatus(request.status());

        Vencimento vencimentoSalvo = vencimentoRepository.save(novoVencimento);

        return new ResponseEntity<>(vencimentoSalvo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<VencimentoDTO> atualizarVencimento(@PathVariable Long id, @RequestBody VencimentoRequestDTO request) {
        Vencimento vencimentoExistente = vencimentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vencimento não encontrado."));
        
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente não encontrado com o ID fornecido."));

        vencimentoExistente.setCliente(cliente);
        vencimentoExistente.setDescricao(request.descricao());
        vencimentoExistente.setDataVencimento(request.dataVencimento());
        vencimentoExistente.setStatus(request.status());

        Vencimento vencimentoAtualizado = vencimentoRepository.save(vencimentoExistente);

        VencimentoDTO dto = new VencimentoDTO(
            vencimentoAtualizado.getId(),
            vencimentoAtualizado.getDescricao(),
            vencimentoAtualizado.getDataVencimento().toString(),
            vencimentoAtualizado.getDataVencimento().toString(),
            vencimentoAtualizado.getStatus().name(),
            vencimentoAtualizado.getCliente().getId(),
            vencimentoAtualizado.getCliente().getRazaoSocial()
        );

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletarVencimento(@PathVariable Long id) {
        if (!vencimentoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        vencimentoRepository.deleteById(id);
        
        return ResponseEntity.noContent().build();
    }
}