package com.notus.contabil.sistema_fiscal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notus.contabil.sistema_fiscal.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByClienteId(Long clienteId);

    boolean existsByClienteIdAndTitulo(Long clienteId, String titulo);

    // Novo método que adicionamos para encontrar a tarefa de revisão específica
    Optional<Task> findByClienteIdAndTitulo(Long clienteId, String titulo);
}