package com.notus.contabil.sistema_fiscal.controller;

import com.notus.contabil.sistema_fiscal.dto.TaskCreateDTO;
import com.notus.contabil.sistema_fiscal.dto.TaskDTO;
import com.notus.contabil.sistema_fiscal.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public List<TaskDTO> listar() {
        return taskService.listar();
    }

    @PostMapping
    public TaskDTO criar(@RequestBody TaskCreateDTO dto) {
        return taskService.criar(dto);
    }

    @PutMapping("/{id}")
    public TaskDTO atualizar(@PathVariable Long id, @RequestBody TaskCreateDTO dto) {
        return taskService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        taskService.deletar(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<?> enviarTaskAoCliente(@PathVariable Long id, @RequestParam String canal) {
        taskService.enviarTaskAoCliente(id, canal);
        return ResponseEntity.ok().build();
    }
}
