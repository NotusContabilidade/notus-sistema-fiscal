package com.notus.contabil.sistema_fiscal.controller;

import com.notus.contabil.sistema_fiscal.entity.Documento;
import com.notus.contabil.sistema_fiscal.entity.Task;
import com.notus.contabil.sistema_fiscal.repository.DocumentoRepository;
import com.notus.contabil.sistema_fiscal.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/painel-controle")
public class PainelControleController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    // DTO para unificar itens no painel
    public record PainelItemDTO(
            Long id,
            String tipo,
            String titulo,
            String descricao,
            String status,
            LocalDate prazo,
            String responsavel,
            Long clienteId,
            String clienteNome,
            LocalDateTime dataCriacao
    ) {}

    @GetMapping("/workflow")
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<List<PainelItemDTO>> getWorkflowItems(@RequestParam(required = false) Long clienteId) {
        List<Task> tasks;
        List<Documento> documentos;

        if (clienteId != null) {
            tasks = taskRepository.findByClienteId(clienteId);
            documentos = documentoRepository.findByClienteIdAndStatus(clienteId, "PENDENTE");
        } else {
            tasks = taskRepository.findAll();
            documentos = documentoRepository.findByStatus("PENDENTE");
        }

        Stream<PainelItemDTO> taskStream = tasks.stream().map(task -> new PainelItemDTO(
                task.getId(),
                "TAREFA",
                task.getTitulo(),
                task.getDescricao(),
                task.getStatus(),
                task.getPrazo(),
                task.getResponsavel(),
                task.getCliente() != null ? task.getCliente().getId() : null,
                task.getCliente() != null ? task.getCliente().getRazaoSocial() : "N/A",
                task.getDataCriacao()
        ));

        Stream<PainelItemDTO> docStream = documentos.stream().map(doc -> new PainelItemDTO(
                doc.getId(),
                "DOCUMENTO",
                "Revisar Documento: " + doc.getNomeArquivo(),
                "Documento enviado pelo cliente para revisão.",
                doc.getStatus(),
                doc.getDataUpload().toLocalDate().plusDays(1),
                "N/A",
                doc.getCliente() != null ? doc.getCliente().getId() : null,
                doc.getCliente() != null ? doc.getCliente().getRazaoSocial() : "N/A",
                doc.getDataUpload()
        ));

        List<PainelItemDTO> combinedList = Stream.concat(taskStream, docStream)
                .sorted(Comparator.comparing(PainelItemDTO::dataCriacao).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(combinedList);
    }
}