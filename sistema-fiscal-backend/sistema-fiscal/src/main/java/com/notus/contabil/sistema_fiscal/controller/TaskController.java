package com.notus.contabil.sistema_fiscal.controller;

import com.notus.contabil.sistema_fiscal.dto.TaskCreateDTO;
import com.notus.contabil.sistema_fiscal.dto.TaskDTO;
import com.notus.contabil.sistema_fiscal.entity.Task;
import com.notus.contabil.sistema_fiscal.repository.TaskRepository;
import com.notus.contabil.sistema_fiscal.services.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // <-- IMPORT ADICIONADO AQUI
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private TaskRepository taskRepository;

    public record StatusUpdateDTO(String status) {}

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public List<TaskDTO> listar() {
        return taskService.listar();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public TaskDTO criar(@RequestBody TaskCreateDTO dto) {
        return taskService.criar(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public TaskDTO atualizar(@PathVariable Long id, @RequestBody TaskCreateDTO dto) {
        return taskService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        taskService.deletar(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cliente/me")
    public ResponseEntity<List<TaskDTO>> listarMinhasTarefas(Authentication authentication) {
        String email = authentication.getName();
        List<TaskDTO> tasks = taskService.listarMinhasTarefas(email);
        return ResponseEntity.ok(tasks);
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
    @Transactional
    public ResponseEntity<TaskDTO> updateTaskStatus(@PathVariable Long id, @RequestBody StatusUpdateDTO dto) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task não encontrada com ID: " + id));
        
        String oldStatus = task.getStatus();
        task.setStatus(dto.status());

        String historicoMsg = String.format("Status alterado de '%s' para '%s' em %s.",
            oldStatus,
            dto.status(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        task.getHistorico().add(historicoMsg);

        if ("CONCLUIDO".equalsIgnoreCase(dto.status())) {
            task.setDataConclusao(LocalDateTime.now());
        } else {
            task.setDataConclusao(null);
        }
        
        Task taskSalva = taskRepository.save(task);
        return ResponseEntity.ok(taskService.toDTO(taskSalva));
    }
}