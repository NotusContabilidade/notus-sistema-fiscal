package com.notus.contabil.sistema_fiscal.repository;

import com.notus.contabil.sistema_fiscal.entity.Comunicado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComunicadoRepository extends JpaRepository<Comunicado, Long> {

    // Busca todos os comunicados de um cliente específico ou comunicados em massa (cliente_id IS NULL)
    List<Comunicado> findByClienteIdOrClienteIdIsNullOrderByDataCriacaoDesc(Long clienteId);

    // Busca os 5 comunicados mais recentes para o painel de controle
    List<Comunicado> findTop5ByOrderByDataCriacaoDesc();
}
