package com.notus.contabil.sistema_fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TarefaRealRepository extends JpaRepository<TarefaReal, Long> {
    
    // Método para evitar a criação de tarefas duplicadas
    boolean existsByTarefaModeloAndClienteAndDataVencimento(
        TarefaModelo tarefaModelo, 
        Cliente cliente, 
        LocalDate dataVencimento
    );
}