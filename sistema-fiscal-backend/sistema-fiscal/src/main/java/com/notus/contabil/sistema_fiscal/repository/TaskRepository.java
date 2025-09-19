package com.notus.contabil.sistema_fiscal.repository;

import com.notus.contabil.sistema_fiscal.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate; // Importe
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByClienteId(Long clienteId);

    // <-- MÉTODO ADICIONADO -->
    // Verifica se uma tarefa para um cliente com um título específico já existe.
    boolean existsByClienteIdAndTitulo(Long clienteId, String titulo);
}