package com.notus.contabil.sistema_fiscal.services;

import com.notus.contabil.sistema_fiscal.dto.TaskCreateDTO;
import com.notus.contabil.sistema_fiscal.dto.TaskDTO;
import com.notus.contabil.sistema_fiscal.entity.Cliente;
import com.notus.contabil.sistema_fiscal.entity.Task;
import com.notus.contabil.sistema_fiscal.repository.TaskRepository;
import com.notus.contabil.sistema_fiscal.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public List<TaskDTO> listar() {
        List<Task> tasks = taskRepository.findAll();
        List<TaskDTO> dtos = new ArrayList<>();
        for (Task t : tasks) {
            dtos.add(toDTO(t));
        }
        return dtos;
    }

    public TaskDTO criar(TaskCreateDTO dto) {
        Task task = new Task();
        task.setTitulo(dto.getTitulo());
        task.setDescricao(dto.getDescricao());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : "pendente");
        task.setPrazo(dto.getPrazo());
        task.setResponsavel(dto.getResponsavel());
        task.setDataCriacao(LocalDateTime.now());
        task.setAnexos(dto.getAnexos());
        if (dto.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
            task.setCliente(cliente);
        }
        task.setHistorico(new ArrayList<>());
        return toDTO(taskRepository.save(task));
    }

    public TaskDTO atualizar(Long id, TaskCreateDTO dto) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task não encontrada"));
        task.setTitulo(dto.getTitulo());
        task.setDescricao(dto.getDescricao());
        task.setStatus(dto.getStatus());
        task.setPrazo(dto.getPrazo());
        task.setResponsavel(dto.getResponsavel());
        task.setAnexos(dto.getAnexos());
        if (dto.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
            task.setCliente(cliente);
        }
        return toDTO(taskRepository.save(task));
    }

    public void deletar(Long id) {
        taskRepository.deleteById(id);
    }

    public void enviarTaskAoCliente(Long taskId, String canal) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task não encontrada"));
        // Lógica para enviar a task ao cliente pelo canal especificado
    }

    // NOVO MÉTODO: Buscar tarefas do cliente autenticado
    public List<TaskDTO> listarMinhasTarefas(String email) {
        Cliente cliente = clienteRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        List<Task> tasks = taskRepository.findByClienteId(cliente.getId());
        List<TaskDTO> dtos = new ArrayList<>();
        for (Task t : tasks) {
            dtos.add(toDTO(t));
        }
        return dtos;
    }

    private TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitulo(task.getTitulo());
        dto.setDescricao(task.getDescricao());
        dto.setStatus(task.getStatus());
        dto.setPrazo(task.getPrazo());
        dto.setResponsavel(task.getResponsavel());
        dto.setDataCriacao(task.getDataCriacao());
        dto.setDataConclusao(task.getDataConclusao());
        dto.setAnexos(task.getAnexos());
        dto.setHistorico(task.getHistorico());
        if (task.getCliente() != null) {
            dto.setClienteId(task.getCliente().getId());
        }
        return dto;
    }
}