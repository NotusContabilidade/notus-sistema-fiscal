package com.notus.contabil.sistema_fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalculoRepository extends JpaRepository<Calculo, Long> {

    List<Calculo> findAllByClienteIdOrderByAnoReferenciaDescMesReferenciaDesc(Long clienteId);
    
    Optional<Calculo> findByClienteIdAndMesReferenciaAndAnoReferencia(Long clienteId, int mesReferencia, int anoReferencia);

    // ✅ ALTERAÇÃO: Removido "simples_nacional."
    @Query(value = "SELECT COALESCE(SUM(c.das_total), 0.0) FROM calculos c WHERE c.ano_referencia = :ano AND c.mes_referencia = :mes",
           nativeQuery = true)
    Double sumDasTotalByAnoAndMes(@Param("ano") int ano, @Param("mes") int mes);
    
    // ✅ ALTERAÇÃO: Removido "simples_nacional." e "public."
    @Query(value = "SELECT c.cliente_id, cl.razao_social, c.das_total " +
                   "FROM calculos c JOIN clientes cl ON c.cliente_id = cl.id " +
                   "WHERE c.ano_referencia = :ano AND c.mes_referencia = :mes",
           nativeQuery = true)
    List<Object[]> findClientesComCalculoNoMes(@Param("ano") int ano, @Param("mes") int mes);
}