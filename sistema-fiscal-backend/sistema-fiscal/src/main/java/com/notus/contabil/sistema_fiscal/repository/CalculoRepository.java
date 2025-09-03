package com.notus.contabil.sistema_fiscal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.notus.contabil.sistema_fiscal.dto.ClienteFinanceiroDTO;
import com.notus.contabil.sistema_fiscal.entity.Calculo;

@Repository
public interface CalculoRepository extends JpaRepository<Calculo, Long> {

    List<Calculo> findAllByClienteIdOrderByAnoReferenciaDescMesReferenciaDesc(Long clienteId);
    
    Optional<Calculo> findByClienteIdAndMesReferenciaAndAnoReferencia(Long clienteId, int mesReferencia, int anoReferencia);

    @Query("SELECT COALESCE(SUM(c.dasTotal), 0.0) FROM Calculo c WHERE c.anoReferencia = :ano AND c.mesReferencia = :mes")
    Double sumDasTotalByAnoAndMes(@Param("ano") int ano, @Param("mes") int mes);
    
    @Query("SELECT new com.notus.contabil.sistema_fiscal.dto.ClienteFinanceiroDTO(cl.id, cl.razaoSocial, c.dasTotal) " +
           "FROM Calculo c JOIN c.cliente cl " +
           "WHERE c.anoReferencia = :ano AND c.mesReferencia = :mes")
    List<ClienteFinanceiroDTO> findClientesComCalculoNoMes(@Param("ano") int ano, @Param("mes") int mes);
}