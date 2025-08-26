package com.notus.contabil.sistema_fiscal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculoRepository extends JpaRepository<Calculo, Long> {

    List<Calculo> findAllByClienteIdOrderByAnoReferenciaDescMesReferenciaDesc(Long clienteId);
    
    Optional<Calculo> findByClienteIdAndMesReferenciaAndAnoReferencia(Long clienteId, int mesReferencia, int anoReferencia);

    @Query(value = "SELECT COALESCE(SUM(c.das_total), 0.0) FROM simples_nacional.calculos c WHERE c.ano_referencia = :ano AND c.mes_referencia = :mes",
           nativeQuery = true)
    Double sumDasTotalByAnoAndMes(@Param("ano") int ano, @Param("mes") int mes);
    
    // ✅ MUDANÇA: O tipo de retorno agora é List<Object[]> para mapeamento manual
    @Query(value = "SELECT c.cliente_id, cl.razao_social, c.das_total " +
                   "FROM simples_nacional.calculos c JOIN public.clientes cl ON c.cliente_id = cl.id " +
                   "WHERE c.ano_referencia = :ano AND c.mes_referencia = :mes",
           nativeQuery = true)
    List<Object[]> findClientesComCalculoNoMes(@Param("ano") int ano, @Param("mes") int mes);
}