package com.notus.contabil.sistema_fiscal.repository;

import com.notus.contabil.sistema_fiscal.entity.TarefaRecorrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TarefaRecorrenteRepository extends JpaRepository<TarefaRecorrente, Long> {
    
    // Método para buscar apenas os moldes de tarefas que estão ativos
    List<TarefaRecorrente> findAllByAtivaTrue();

    // Método que estava faltando: Busca todos os moldes de tarefa para um cliente específico.
    List<TarefaRecorrente> findByClienteId(Long clienteId);
}