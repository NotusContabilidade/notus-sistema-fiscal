package com.notus.contabil.sistema_fiscal.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notus.contabil.sistema_fiscal.entity.Cliente;
import com.notus.contabil.sistema_fiscal.entity.TarefaRecorrente;
import com.notus.contabil.sistema_fiscal.repository.ClienteRepository;
import com.notus.contabil.sistema_fiscal.repository.TarefaRecorrenteRepository;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/recorrencias")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
public class TarefaRecorrenteController {

    @Autowired
    private TarefaRecorrenteRepository recorrenteRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // --- DTOs para a comunicação com o Frontend ---

    // DTO para CRIAR/ATUALIZAR uma tarefa recorrente (o que vem do frontend)
    public record TarefaRecorrenteRequestDTO(
            Long clienteId, String titulo, String descricao, String categoria,
            String responsavel, TarefaRecorrente.Frequencia frequencia, int diaVencimento, boolean ativa
    ) {}

    // DTO para ENVIAR dados para o frontend (o que o frontend recebe)
    public record TarefaRecorrenteResponseDTO(
            Long id, Long clienteId, String titulo, String descricao, String categoria,
            String responsavel, TarefaRecorrente.Frequencia frequencia, int diaVencimento, boolean ativa
    ) {}


    // --- MÉTODOS DO CONTROLLER ATUALIZADOS PARA USAR DTOs ---

    @GetMapping("/cliente/{clienteId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<TarefaRecorrenteResponseDTO>> listarPorCliente(@PathVariable Long clienteId) {
        List<TarefaRecorrente> recorrencias = recorrenteRepository.findByClienteId(clienteId);
        List<TarefaRecorrenteResponseDTO> dtos = recorrencias.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<TarefaRecorrenteResponseDTO> criar(@RequestBody TarefaRecorrenteRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        TarefaRecorrente novaRecorrencia = new TarefaRecorrente();
        novaRecorrencia.setCliente(cliente);
        novaRecorrencia.setTitulo(dto.titulo());
        novaRecorrencia.setDescricao(dto.descricao());
        novaRecorrencia.setCategoria(dto.categoria());
        novaRecorrencia.setResponsavel(dto.responsavel());
        novaRecorrencia.setFrequencia(dto.frequencia());
        novaRecorrencia.setDiaVencimento(dto.diaVencimento());
        novaRecorrencia.setAtiva(dto.ativa());

        TarefaRecorrente salva = recorrenteRepository.save(novaRecorrencia);
        return ResponseEntity.ok(toResponseDTO(salva));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<TarefaRecorrenteResponseDTO> atualizar(@PathVariable Long id, @RequestBody TarefaRecorrenteRequestDTO dto) {
        TarefaRecorrente recorrencia = recorrenteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa recorrente não encontrada"));

        recorrencia.setTitulo(dto.titulo());
        recorrencia.setDescricao(dto.descricao());
        recorrencia.setCategoria(dto.categoria());
        recorrencia.setResponsavel(dto.responsavel());
        recorrencia.setFrequencia(dto.frequencia());
        recorrencia.setDiaVencimento(dto.diaVencimento());
        recorrencia.setAtiva(dto.ativa());

        TarefaRecorrente salva = recorrenteRepository.save(recorrencia);
        return ResponseEntity.ok(toResponseDTO(salva));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!recorrenteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        recorrenteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Método auxiliar para converter a Entidade em DTO de resposta
    private TarefaRecorrenteResponseDTO toResponseDTO(TarefaRecorrente entity) {
        return new TarefaRecorrenteResponseDTO(
                entity.getId(),
                entity.getCliente().getId(),
                entity.getTitulo(),
                entity.getDescricao(),
                entity.getCategoria(),
                entity.getResponsavel(),
                entity.getFrequencia(),
                entity.getDiaVencimento(),
                entity.isAtiva()
        );
    }
}