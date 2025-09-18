package com.notus.contabil.sistema_fiscal.controller;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.notus.contabil.sistema_fiscal.repository.DocumentoRepository;
import com.notus.contabil.sistema_fiscal.repository.TaskRepository;

@RestController
@RequestMapping("/api/painel-controle")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
public class PainelControleController {

    @Autowired private TaskRepository taskRepository;
    @Autowired private DocumentoRepository documentoRepository;

    public record WorkflowItemDTO(
        Long id,
        String tipo, // "TAREFA" ou "DOCUMENTO"
        String titulo,
        String status,
        LocalDate prazo,
        Long clienteId,
        String clienteNome
    ) {}

    @GetMapping("/workflow")
    @Transactional(readOnly = true)
    public ResponseEntity<List<WorkflowItemDTO>> getWorkflowItems(
        @RequestParam(required = false) Long clienteId
    ) {
        Stream<WorkflowItemDTO> tasksStream = (clienteId == null 
            ? taskRepository.findAll() 
            : taskRepository.findByClienteId(clienteId))
            .stream()
            .map(task -> new WorkflowItemDTO(
                task.getId(), 
                "TAREFA", 
                task.getTitulo(), 
                task.getStatus(), 
                task.getPrazo(),
                task.getCliente().getId(),
                task.getCliente().getRazaoSocial()
            ));

        Stream<WorkflowItemDTO> documentosStream = (clienteId == null
            ? documentoRepository.findAll()
            : documentoRepository.findByClienteId(clienteId))
            .stream()
            .filter(doc -> "PENDENTE".equalsIgnoreCase(doc.getStatus()))
            .map(doc -> new WorkflowItemDTO(
                doc.getId(), 
                "DOCUMENTO", 
                "Revisar: " + doc.getNomeArquivo(),
                doc.getStatus(),
                null, 
                doc.getCliente().getId(),
                doc.getCliente().getRazaoSocial()
            ));

        List<WorkflowItemDTO> workflowItems = Stream.concat(tasksStream, documentosStream)
            .sorted(Comparator.comparing(WorkflowItemDTO::prazo, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        return ResponseEntity.ok(workflowItems);
    }
}