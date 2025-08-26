package com.notus.contabil.sistema_fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VencimentoRepository extends JpaRepository<Vencimento, Long> {

    // ✅ CORREÇÃO FINAL: Mudamos para uma Query Nativa (SQL puro do PostgreSQL)
    // A diferença chave é o `c.razao_social::text`. Esta é uma sintaxe específica do
    // PostgreSQL para FORÇAR a conversão da coluna para texto antes de usar a função LOWER().
    // Isso resolve o erro "lower(bytea)" de forma definitiva no nível da aplicação.
    @Query(value = "SELECT v.* FROM simples_nacional.vencimentos v JOIN public.clientes c ON v.cliente_id = c.id WHERE " +
                   "v.data_vencimento BETWEEN :inicio AND :fim AND " +
                   "(:filtro IS NULL OR LOWER(c.razao_social::text) LIKE LOWER(CONCAT('%', :filtro, '%')) OR c.cnpj LIKE CONCAT('%', :filtro, '%'))",
           nativeQuery = true)
    List<Vencimento> findVencimentosComFiltro(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("filtro") String filtro
    );
    
    List<Vencimento> findAllByClienteIdOrderByDataVencimentoDesc(Long clienteId);
}