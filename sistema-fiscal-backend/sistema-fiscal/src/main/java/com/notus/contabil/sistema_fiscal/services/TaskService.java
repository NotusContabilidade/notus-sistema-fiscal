package com.notus.contabil.sistema_fiscal.services;

import com.notus.contabil.sistema_fiscal.entity.Task;
import com.notus.contabil.sistema_fiscal.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> listarTarefas() {
        return taskRepository.findAll();
    }

    public Task salvar(Task task) {
        return taskRepository.save(task);
    }

    public void deletar(Long id) {
        taskRepository.deleteById(id);
    }

    public Task buscarPorId(Long id) {
        return taskRepository.findById(id).orElse(null);
    }
}