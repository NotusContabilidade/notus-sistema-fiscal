package com.notus.contabil.sistema_fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VencimentoRepository extends JpaRepository<Vencimento, Long> {

    // ✅ REATORADO PARA JPQL - AGORA É SEGURO!
    // A query agora opera sobre as entidades Vencimento (v) e Cliente (c).
    // O Hibernate aplicará o filtro de tenant_id tanto em 'v' quanto em 'c'
    // automaticamente, garantindo que a busca só retorne resultados
    // do escritório (tenant) que fez a requisição.
    @Query("SELECT v FROM Vencimento v JOIN v.cliente c WHERE " +
           "v.dataVencimento BETWEEN :inicio AND :fim AND " +
           "(:filtro IS NULL OR LOWER(c.razaoSocial) LIKE LOWER(CONCAT('%', :filtro, '%')) OR c.cnpj LIKE CONCAT('%', :filtro, '%'))")
    List<Vencimento> findVencimentosComFiltro(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("filtro") String filtro
    );
    
    List<Vencimento> findAllByClienteIdOrderByDataVencimentoDesc(Long clienteId);
}