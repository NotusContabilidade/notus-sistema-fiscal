package com.notus.contabil.sistema_fiscal.controller;

import com.notus.contabil.sistema_fiscal.dto.TaskDTO;
import com.notus.contabil.sistema_fiscal.dto.TaskCreateDTO;
import com.notus.contabil.sistema_fiscal.entity.Task;
import com.notus.contabil.sistema_fiscal.entity.User;
import com.notus.contabil.sistema_fiscal.services.TaskService;
import com.notus.contabil.sistema_fiscal.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<TaskDTO> listarTarefas() {
        return taskService.listarTarefas().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public TaskDTO criarTarefa(@RequestBody TaskCreateDTO dto) {
        Task task = new Task();
        task.setTitulo(dto.getTitulo());
        task.setDescricao(dto.getDescricao());
        task.setStatus(dto.getStatus());
        task.setPrazo(dto.getPrazo());
        if (dto.getResponsavelId() != null) {
            User responsavel = userRepository.findById(dto.getResponsavelId()).orElse(null);
            task.setResponsavel(responsavel);
        }
        task.setCriadoEm(LocalDateTime.now());
        task.setAtualizadoEm(LocalDateTime.now());
        Task saved = taskService.salvar(task);
        return toDTO(saved);
    }

    @GetMapping("/{id}")
    public TaskDTO buscarPorId(@PathVariable Long id) {
        Task task = taskService.buscarPorId(id);
        return task != null ? toDTO(task) : null;
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        taskService.deletar(id);
    }

    private TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitulo(task.getTitulo());
        dto.setDescricao(task.getDescricao());
        dto.setStatus(task.getStatus());
        dto.setPrazo(task.getPrazo());
        dto.setResponsavelId(task.getResponsavel() != null ? task.getResponsavel().getId() : null);
        dto.setCriadoEm(task.getCriadoEm());
        dto.setAtualizadoEm(task.getAtualizadoEm());
        return dto;
    }
}
